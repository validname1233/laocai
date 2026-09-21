package indi.kyson.laocai.app.conversation

import indi.kyson.laocai.bot.annotation.Filter
import indi.kyson.laocai.bot.annotation.Listener
import indi.kyson.laocai.bot.annotation.MatchType
import indi.kyson.laocai.bot.annotation.MultiFilter
import indi.kyson.laocai.bot.event.GroupMessageEvent
import indi.kyson.laocai.app.media.AudioReplyService
import org.springframework.stereotype.Component

@Component
class AiHandler(
    private val aiConversationService: AiConversationService,
    private val audioReplyService: AudioReplyService,
) {
    @Listener
    @Filter(
        targets = [Filter.Targets(mentionBot = true)],
    )
    @MultiFilter(
        value = [Filter(value = "/audio", matchType = MatchType.STARTS_WITH)],
        type = MultiFilter.Type.NONE,
    )
    fun handleMentionBot(event: GroupMessageEvent) = aiConversationService.handle(event)

    @Listener
    @Filter(
        value = "/audio",
        matchType = MatchType.STARTS_WITH,
        targets = [Filter.Targets(mentionBot = true)],
    )
    fun handleAudio(event: GroupMessageEvent) = audioReplyService.handle(event)
}
