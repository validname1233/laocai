package indi.kyson.laocai.handler

import indi.kyson.laocai.ai.ChatClientFactory
import indi.kyson.laocai.ai.GPTSoVITSClient
import indi.kyson.laocai.ai.model.ChatRecord
import indi.kyson.laocai.ai.model.ReplyDecision
import indi.kyson.laocai.bot.Bot
import indi.kyson.laocai.bot.annotation.Filter
import indi.kyson.laocai.bot.annotation.Listener
import indi.kyson.laocai.bot.event.GroupMessageEvent
import indi.kyson.laocai.bot.event.MessageEvent
import indi.kyson.laocai.bot.segment.IncomingImageSegment
import indi.kyson.laocai.bot.segment.OutgoingRecordSegment
import indi.kyson.laocai.bot.segment.TextSegment
import org.slf4j.LoggerFactory
import org.springframework.ai.content.Media
import org.springframework.core.io.FileSystemResource
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.util.MimeType
import org.springframework.util.MimeTypeUtils
import redis.clients.jedis.RedisClient
import tools.jackson.databind.json.JsonMapper
import java.io.BufferedInputStream
import java.io.IOException
import java.net.URI
import java.net.URLConnection
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@Component
class AiHandler(
    private val chatClientFactory: ChatClientFactory,
    private val bot: Bot,
    private val redisClient: RedisClient,
    private val jsonMapper: JsonMapper,
    private val ttsClient: GPTSoVITSClient,
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @Listener
    @Filter("(?s)(?!/audio\\b).*")
    fun test(event: GroupMessageEvent) {
        val groupId = event.group.groupId
        val content = event.plainText

        val response = bot.getUserProfile(event.selfId).block() ?: run {
            log.error("AI的决定为空")
            return
        }
        val nickname = response.data?.nickname ?: return

        log.info("收到群消息: {}", content)

        val imageIds = cacheImages(event)

        appendMessage(groupId, ChatRecord(
            event.time,
            event.senderId,
            content,
            imageIds,
        ))

        val messages = redisClient.lrange("event:incoming-group-message:%d".format(groupId), 0, -1)
            .map { json -> jsonMapper.readValue(json, ChatRecord::class.java) }

        val historyText = messages
            .joinToString("\n") { record ->
                val imgPart = if (record.imageIds.isEmpty())
                    ""
                else
                    " " + "[图片]".repeat(record.imageIds.size)
                "[%s] %s: %s%s".format(
                    TIME_FORMATTER.format(Instant.ofEpochSecond(record.time)),
                    record.senderId,
                    record.content.replace('\n', ' '),
                    imgPart,
                )
            }

        val images = messages.asSequence()
            .flatMap { it.imageIds.asSequence() }
            .mapNotNull { resourceId ->
                val path = IMAGE_CACHE_DIR.resolve(resourceId)
                if (Files.exists(path)) path else null
            }
            .map { path ->
                Media.builder()
                    .mimeType(detectMime(path))
                    .data(FileSystemResource(path))
                    .build()
            }
            .toList()
            .toTypedArray()

        val decision = chatClientFactory.getReplyDecisionClient(groupId)
            .prompt()
            .user { spec ->
                spec.text("""
                    下面是 QQ 群最新 50 条消息，你的QQ号是${ event.selfId}, 昵称是$nickname，请判断是否需要作为群友发言

                    消息格式：
                    [发送时间] QQ号: 内容文字 [图片][图片]...

                    $historyText

                    文中每出现一个 [图片] 占位符就对应一张按顺序上传的图片
                """.trimIndent())
                    .media(*images)
            }
            .call()
            .entity(ReplyDecision::class.java) ?: return

        if (!decision.shouldReply) {
            log.info("AI 决定不回消息")
            return
        }

        val aiResponse = chatClientFactory.getChatPersonaClient(groupId)
            .prompt()
            .user { spec ->
                spec.text("""
                    下面是 QQ 群最新 50 条消息。你已经被允许发言，你的QQ号是${ event.selfId}, 昵称是$nickname，请根据群聊气氛生成一句要发送到群里的回复。

                    消息格式：
                    [发送时间] QQ号: 内容文字 [图片][图片]...

                    $historyText

                    文中每出现一个 [图片] 占位符就对应一张按顺序上传的图片
                """.trimIndent())
                    .media(*images)
            }
            .call()
            .content() ?: return

        if (aiResponse.isBlank()) {
            return
        }

        val trimmed = aiResponse.trim()

        bot.sendGroupMsg(groupId, listOf(TextSegment.of(trimmed))).block()

        appendMessage(groupId, ChatRecord(
            Instant.now().epochSecond,
            event.selfId,
            trimmed,
            emptyList(),
        ))
    }

    @Listener
    @Filter("(?s)/audio\\b.*")
    fun handleAudio(event: GroupMessageEvent) {
        val groupId = event.group.groupId

        val prompt = event.plainText.substring(AUDIO_COMMAND.length).trim()
        log.info("收到 /audio 命令: groupId={} prompt={}", groupId, prompt)

        if (prompt.isEmpty()) {
            bot.sendGroupMsg(groupId, listOf(
                TextSegment.of("用法：/audio 你想让洛琪希说的话"),
            )).block()
            return
        }

        val reply = chatClientFactory.getRoxyVoiceClient(groupId)
            .prompt()
            .user(prompt)
            .call()
            .content() ?: run {
            log.error("洛琪希人格回复为空: groupId={}", groupId)
            bot.sendGroupMsg(groupId, listOf(TextSegment.of("生成回复失败了，再试一次吧"))).block()
            return
        }

        if (reply.isBlank()) {
            log.error("洛琪希人格回复为空: groupId={}", groupId)
            bot.sendGroupMsg(groupId, listOf(TextSegment.of("生成回复失败了，再试一次吧"))).block()
            return
        }

        val trimmed = reply.trim()
        log.info("洛琪希回复: {}", trimmed)

        val audio = ttsClient.synthesize(trimmed).block()

        if (audio == null) {
            bot.sendGroupMsg(groupId, listOf(
                TextSegment.of("语音合成失败了，先把文字给你：\n$trimmed"),
            )).block()
            return
        }

        bot.sendGroupMsg(groupId, listOf(
            OutgoingRecordSegment.of("file://$audio"),
        )).block()
    }

    private fun appendMessage(groupId: Long, record: ChatRecord) {
        val key = "event:incoming-group-message:%d".format(groupId)
        val json = jsonMapper.writeValueAsString(record)

        redisClient.multi().use { transaction ->
            transaction.rpush(key, json)
            transaction.ltrim(key, -MESSAGE_WINDOW_SIZE.toLong(), -1)
            transaction.exec()
        }
    }

    private fun cacheImages(event: MessageEvent): List<String> {
        return event.segments.asSequence()
            .filterIsInstance<IncomingImageSegment>()
            .mapNotNull { segment ->
                val resourceId = segment.resourceId
                val url = segment.tempUrl
                if (resourceId.isBlank() || url.isBlank()) {
                    return@mapNotNull null
                }
                try {
                    Files.createDirectories(IMAGE_CACHE_DIR)
                    val filePath = IMAGE_CACHE_DIR.resolve(resourceId)
                    if (!Files.exists(filePath)) {
                        URI.create(url).toURL().openStream().use { input ->
                            Files.copy(input, filePath)
                        }
                        log.info("缓存图片: rid={} -> {}", resourceId, filePath)
                    }
                    resourceId
                } catch (e: Exception) {
                    log.warn("下载图片失败: rid={} url={}", resourceId, url, e)
                    null
                }
            }
            .toList()
    }

    private fun detectMime(path: Path): MimeType {
        return try {
            BufferedInputStream(Files.newInputStream(path)).use { input ->
                val contentType = URLConnection.guessContentTypeFromStream(input)
                if (contentType != null) {
                    MimeType.valueOf(contentType)
                } else {
                    MimeTypeUtils.IMAGE_JPEG
                }
            }
        } catch (e: IOException) {
            log.warn("识别图片 MIME 失败: {}", path, e)
            MimeTypeUtils.IMAGE_JPEG
        }
    }

    @Scheduled(cron = "0 0 3 * * *")
    fun cleanImageCache() {
        ttsClient.cleanExpiredOutputs()

        if (!Files.isDirectory(IMAGE_CACHE_DIR)) {
            return
        }
        val threshold = Instant.now().minus(IMAGE_CACHE_TTL_DAYS, ChronoUnit.DAYS)
        try {
            Files.list(IMAGE_CACHE_DIR).use { stream ->
                var deleted = 0
                stream.forEach { p ->
                    try {
                        if (Files.getLastModifiedTime(p).toInstant().isBefore(threshold)) {
                            Files.delete(p)
                            deleted++
                        }
                    } catch (e: Exception) {
                        log.warn("删除过期图片失败: {}", p, e)
                    }
                }
                if (deleted > 0) {
                    log.info("清理图片缓存: 删除 {} 个过期文件", deleted)
                }
            }
        } catch (e: Exception) {
            log.warn("扫描图片缓存目录失败", e)
        }
    }

    companion object {
        private const val MESSAGE_WINDOW_SIZE = 50
        private val TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.systemDefault())
        private val IMAGE_CACHE_DIR: Path = Paths.get("tmp/images")
        private const val IMAGE_CACHE_TTL_DAYS = 2L
        private const val AUDIO_COMMAND = "/audio"
    }
}
