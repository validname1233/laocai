package indi.kyson.laocai.handler

import indi.kyson.laocai.ai.model.ChatHistory
import indi.kyson.laocai.bot.annotation.Filter
import indi.kyson.laocai.bot.annotation.Listener
import indi.kyson.laocai.bot.event.GroupMessageEvent
import indi.kyson.laocai.bot.segment.TextSegment
import indi.kyson.laocai.service.ChatHistoryService
import indi.kyson.laocai.service.ImageCacheService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class ChatHistoryHandler(
    private val chatHistoryService: ChatHistoryService,
    private val imageCacheService: ImageCacheService,
    private val bot: indi.kyson.laocai.bot.Bot,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Listener(priority = -100)
    fun handle(event: GroupMessageEvent) {
        val cached = imageCacheService.cache(event)
        if (imageCacheService.containsGif(event)) {
            log.info("忽略包含 GIF 的群消息: groupId={} messageSeq={} cachedImages={}",
                event.group.groupId, event.messageSeq, cached)
            return
        }
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
