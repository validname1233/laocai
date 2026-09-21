package indi.kyson.laocai.app.history

import org.springframework.stereotype.Service
import redis.clients.jedis.RedisClient
import tools.jackson.databind.json.JsonMapper

@Service
class ChatHistoryService(
    private val redisClient: RedisClient,
    private val jsonMapper: JsonMapper,
) {
    fun append(groupId: Long, history: ChatHistory) {
        val key = key(groupId)
        redisClient.multi().use { transaction ->
            transaction.rpush(key, jsonMapper.writeValueAsString(history))
            transaction.ltrim(key, -MESSAGE_WINDOW_SIZE.toLong(), -1)
            transaction.exec()
        }
    }

    fun load(groupId: Long): List<ChatHistory> = redisClient.lrange(key(groupId), 0, -1)
        .map { jsonMapper.readValue(it, ChatHistory::class.java) }

    private fun key(groupId: Long) = "event:incoming-group-message:$groupId"

    companion object { private const val MESSAGE_WINDOW_SIZE = 50 }
}
