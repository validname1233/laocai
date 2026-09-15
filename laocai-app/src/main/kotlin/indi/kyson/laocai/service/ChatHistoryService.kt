package indi.kyson.laocai.service

import indi.kyson.laocai.ai.model.ChatRecord
import org.springframework.ai.content.Media
import org.springframework.core.io.FileSystemResource
import org.springframework.stereotype.Service
import redis.clients.jedis.RedisClient
import tools.jackson.databind.json.JsonMapper
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Service
class ChatHistoryService(
    private val redisClient: RedisClient,
    private val jsonMapper: JsonMapper,
) {
    fun append(groupId: Long, record: ChatRecord) {
        val key = key(groupId)
        val json = jsonMapper.writeValueAsString(record)
        redisClient.multi().use { transaction ->
            transaction.rpush(key, json)
            transaction.ltrim(key, -MESSAGE_WINDOW_SIZE.toLong(), -1)
            transaction.exec()
        }
    }

    fun load(groupId: Long): List<ChatRecord> =
        redisClient.lrange(key(groupId), 0, -1)
            .map { jsonMapper.readValue(it, ChatRecord::class.java) }

    fun format(records: List<ChatRecord>): String = records.joinToString("\n") { record ->
        val imagePart = if (record.imageIds.isEmpty()) "" else " " + "[Image]".repeat(record.imageIds.size)
        "[%s] %s: %s%s".format(
            TIME_FORMATTER.format(Instant.ofEpochSecond(record.time)),
            record.senderId,
            record.content.replace('\n', ' '),
            imagePart,
        )
    }

    fun resolveImages(records: List<ChatRecord>, imageCache: ImageCacheService): Array<Media> =
        records.asSequence()
            .flatMap { it.imageIds.asSequence() }
            .mapNotNull { imageCache.pathOf(it) }
            .map { path ->
                Media.builder()
                    .mimeType(imageCache.detectMime(path))
                    .data(FileSystemResource(path))
                    .build()
            }
            .toList()
            .toTypedArray()

    private fun key(groupId: Long) = "event:incoming-group-message:%d".format(groupId)

    companion object {
        private const val MESSAGE_WINDOW_SIZE = 50
        private val TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.systemDefault())
    }
}
