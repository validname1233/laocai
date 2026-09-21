package indi.kyson.laocai.app.conversation

import com.embabel.agent.api.common.AgentImage
import indi.kyson.laocai.app.history.ChatHistory
import indi.kyson.laocai.app.history.ChatHistoryService
import indi.kyson.laocai.app.media.GifMessagePolicy
import indi.kyson.laocai.app.media.ImageCacheService
import indi.kyson.laocai.app.profile.UserProfileService
import indi.kyson.laocai.bot.Bot
import indi.kyson.laocai.bot.event.GroupMessageEvent
import indi.kyson.laocai.bot.segment.MentionSegment
import indi.kyson.laocai.bot.segment.TextSegment
import org.slf4j.LoggerFactory
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.stereotype.Service
import tools.jackson.databind.json.JsonMapper
import java.time.Duration
import java.time.Instant

@Service
class AiConversationService(
    private val groupReplyGenerator: GroupReplyGenerator,
    private val bot: Bot,
    private val chatHistoryService: ChatHistoryService,
    private val imageCacheService: ImageCacheService,
    private val gifMessagePolicy: GifMessagePolicy,
    private val userProfileService: UserProfileService,
    private val historyRenderer: ChatHistoryRenderer,
    private val jsonMapper: JsonMapper,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun handle(event: GroupMessageEvent) {
        if (gifMessagePolicy.isUnusable(event)) return
        val groupId = event.group.groupId
        val reply = try {
            generateReply(groupId)
        } catch (e: Exception) {
            log.error("AI 生成回复失败: groupId={}", groupId, e)
            notifyFailure(groupId)
            return
        }

        if (!sendReply(groupId, reply)) return

        // Sending and saving are separate: a Redis failure must never retransmit a delivered reply.
        runCatching {
            chatHistoryService.append(groupId, ChatHistory(Instant.now().epochSecond, event.selfId, listOf(TextSegment.of(reply))))
        }.onFailure { log.error("写入 AI 回复历史失败: groupId={}", groupId, it) }
    }

    private fun generateReply(groupId: Long): String {
        // 根据 groupId 从 Redis 中加载历史
        val histories = chatHistoryService.load(groupId)
        // 获取历史中所有 user 的 id
        val userIds = histories.flatMap { history ->
            listOf(history.senderId) + history.segments.filterIsInstance<MentionSegment>().map { it.userId }
        }.toSet()
        // 获取 user 在该群中的昵称
        val names: Map<Long, String> = userProfileService.resolve(groupId, userIds)
        // 获取聊天历史中所有图片的真实 url
        val imageReferences = histories.flatMap { it.segments }.mapNotNull(ChatHistoryRenderer::referenceOf)
        // 先把图片读入内存，避免调用期间缓存文件被清理导致文本和附件错位
        val available = imageCacheService.readAvailable(imageReferences)
        // 将聊天历史渲染成 AI 可读的提示词，同时建立全局图片编号到资源引用的稳定顺序
        val rendered = historyRenderer.render(histories, names, available.keys)
        val images = rendered.imageResources.map { reference ->
            val bytes = available.getValue(reference)
            AgentImage.create(imageCacheService.detectMime(bytes).toString(), bytes)
        }
        return groupReplyGenerator
            .generate(GroupConversationContext(rendered.text, images))
            .trim()
            .takeIf(String::isNotEmpty)
            ?: throw IllegalStateException("AI 回复为空")
    }

    /** Only an explicit Milky success confirms delivery. Transport ambiguity is not a rejection. */
    private fun sendReply(groupId: Long, reply: String): Boolean {
        val response = try {
            bot.sendGroupMsg(groupId, listOf(TextSegment.of(reply))).block(SEND_TIMEOUT)
        } catch (e: WebClientResponseException) {
            if (e.statusCode.is4xxClientError) {
                log.warn("正常回复被拒绝: groupId={} status={}", groupId, e.statusCode)
                notifyFailure(groupId)
            } else {
                log.warn("正常回复发送结果未知: groupId={}", groupId, e)
            }
            return false
        } catch (e: Exception) {
            log.warn("正常回复发送结果未知，不追加发送或入库: groupId={} ", groupId, e)
            return false
        }

        val result = response?.let { runCatching { jsonMapper.readTree(it) }.getOrNull() }
        return when {
            result?.get("status")?.asString() == "ok" && result.get("retcode")?.asLong() == 0L -> true
            result?.get("status")?.asString() == "failed" -> {
                log.warn("Milky 明确返回发送失败: groupId={}", groupId)
                notifyFailure(groupId)
                false
            }
            else -> {
                log.warn("正常回复没有有效发送确认，不追加发送或入库: groupId={}", groupId)
                false
            }
        }
    }

    private fun notifyFailure(groupId: Long) {
        runCatching { bot.sendGroupMsg(groupId, listOf(TextSegment.of("AI回复失败"))).block(SEND_TIMEOUT) }
            .onFailure { log.error("发送 AI 失败提示失败: groupId={}", groupId, it) }
    }

    companion object {
        private val SEND_TIMEOUT = Duration.ofSeconds(30)
    }
}
