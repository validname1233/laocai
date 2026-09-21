package indi.kyson.laocai.app.history

import indi.kyson.laocai.app.media.GifMessagePolicy
import indi.kyson.laocai.app.media.ImageCacheService
import indi.kyson.laocai.bot.Bot
import indi.kyson.laocai.bot.annotation.Listener
import indi.kyson.laocai.bot.event.GroupMessageEvent
import indi.kyson.laocai.bot.segment.TextSegment
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class ChatHistoryHandler(
    private val chatHistoryService: ChatHistoryService,
    private val imageCacheService: ImageCacheService,
    private val gifMessagePolicy: GifMessagePolicy,
    private val bot: Bot,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Listener(priority = -100)
    fun handle(event: GroupMessageEvent) {
        val cached = imageCacheService.cache(event)
        if (gifMessagePolicy.isUnusable(event)) return
        val history = ChatHistory(event.time, event.senderId, event.segments)
        var lastError: Exception? = null
        repeat(MAX_ATTEMPTS) { attempt ->
            try {
                chatHistoryService.append(event.group.groupId, history)
                return
            } catch (e: Exception) {
                lastError = e
                if (attempt + 1 < MAX_ATTEMPTS) Thread.sleep(RETRY_DELAY_MS)
            }
        }
        log.error("写入群聊历史失败: groupId={} senderId={} cachedImages={}", 
            event.group.groupId, event.senderId, cached, lastError)
        if (event.mentionedUserIds.contains(event.selfId)) {
            runCatching { 
                bot.sendGroupMsg(event.group.groupId, 
                    listOf(TextSegment.of("好像出了点问题.."))
                ).block() 
            }.onFailure { log.error("发送历史写入失败提示失败", it) }
        }
    }

    companion object { private const val MAX_ATTEMPTS = 3; private const val RETRY_DELAY_MS = 100L }
}
