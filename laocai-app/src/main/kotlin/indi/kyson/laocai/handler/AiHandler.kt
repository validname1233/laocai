package indi.kyson.laocai.handler

import indi.kyson.laocai.bot.annotation.Filter
import indi.kyson.laocai.bot.annotation.Listener
import indi.kyson.laocai.bot.event.GroupMessageEvent
import indi.kyson.laocai.service.AiConversationService
import indi.kyson.laocai.service.AudioReplyService
import org.springframework.stereotype.Component

@Component
class AiHandler(
    private val aiConversationService: AiConversationService,
    private val audioReplyService: AudioReplyService,
) {
    @Listener
    @Filter(
        value = "(?s)(?!/audio\\b).*",
        targets = [Filter.Targets(mentionBot = true)],
    )
    fun proacive(event: GroupMessageEvent) = aiConversationService.handle(event)

    @Listener
    @Filter(
        value = "(?s)/audio\\b.*",
        targets = [Filter.Targets(mentionBot = true)],
    )
    fun handleAudio(event: GroupMessageEvent) = audioReplyService.handle(event)
}
