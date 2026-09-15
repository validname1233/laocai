package indi.kyson.laocai.service

import indi.kyson.laocai.ai.ChatClientFactory
import indi.kyson.laocai.ai.model.ChatRecord
import indi.kyson.laocai.bot.Bot
import indi.kyson.laocai.bot.event.GroupMessageEvent
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class AiConversationService(
    private val chatClientFactory: ChatClientFactory,
    private val bot: Bot,
    private val chatHistoryService: ChatHistoryService,
    private val imageCacheService: ImageCacheService,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun handle(event: GroupMessageEvent) {
        val groupId = event.group.groupId
        val content = event.plainText
        val response = bot.getUserProfile(event.selfId).block() ?: run {
            log.error("AI的决定为空")
            return
        }
        response.data?.nickname ?: return
        log.info("收到群消息: {}", content)
        val imageIds = imageCacheService.cache(event)
        chatHistoryService.append(groupId, ChatRecord(event.time, event.senderId, content, imageIds))
        val messages = chatHistoryService.load(groupId)
        chatHistoryService.format(messages)
        chatHistoryService.resolveImages(messages, imageCacheService)
    }
}
