package indi.kyson.laocai.app.media

import indi.kyson.laocai.bot.event.MessageEvent
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * 含 GIF 的消息无法交给配置的 AI 模型，因此整条消息不可用：既不写入群聊历史，也不触发回复。
 *
 * 群聊历史的入站监听器和群聊回复的入站监听器共用这条规则，判定与原因说明只在这里维护。
 */
@Component
class GifMessagePolicy(private val imageCache: ImageCacheService) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun isUnusable(event: MessageEvent): Boolean {
        if (!imageCache.containsGif(event)) return false
        log.info("忽略包含 GIF 的群消息: peerId={} messageSeq={}", event.peerId, event.messageSeq)
        return true
    }
}
