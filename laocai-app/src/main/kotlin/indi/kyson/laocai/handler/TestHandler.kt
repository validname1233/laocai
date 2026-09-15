package indi.kyson.laocai.handler

import indi.kyson.laocai.bot.annotation.Filter
import indi.kyson.laocai.bot.annotation.Listener
import indi.kyson.laocai.bot.event.FriendMessageEvent
import indi.kyson.laocai.bot.event.GroupMessageEvent
import indi.kyson.laocai.service.TestMessageService
import org.springframework.stereotype.Component

@Component
class TestHandler(private val testMessageService: TestMessageService) {
    @Listener
    @Filter(
        value = "^[1-9]\\d{4,11}@qq\\.com(\\.cn)?$",
        targets = [Filter.Targets(groups = [634550174L])],
    )
    fun handleGroup(event: GroupMessageEvent) = testMessageService.handleGroup(event)

    @Listener
    fun handleFriend(event: FriendMessageEvent) = testMessageService.handleFriend(event)
}
