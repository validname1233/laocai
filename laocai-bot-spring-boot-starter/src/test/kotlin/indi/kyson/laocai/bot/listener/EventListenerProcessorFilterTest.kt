package indi.kyson.laocai.bot.listener

import indi.kyson.laocai.bot.annotation.Filter
import indi.kyson.laocai.bot.annotation.Listener
import indi.kyson.laocai.bot.annotation.MatchType
import indi.kyson.laocai.bot.annotation.MultiFilter
import indi.kyson.laocai.bot.entity.GroupEntity
import indi.kyson.laocai.bot.entity.GroupMemberEntity
import indi.kyson.laocai.bot.enums.Role
import indi.kyson.laocai.bot.enums.Sex
import indi.kyson.laocai.bot.event.GroupMessageEvent
import indi.kyson.laocai.bot.segment.MentionSegment
import indi.kyson.laocai.bot.segment.TextSegment
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class EventListenerProcessorFilterTest {

    @Test
    fun multiFilterNoneExcludesMatchingPrefix() {
        val handler = Handler()
        val method = Handler::class.java.getDeclaredMethod("handle", GroupMessageEvent::class.java)
        val resolver = EventListenerProcessor().process("handler", method) { handler }
        val dispatcher = EventDispatcher()
        resolver.resolve(dispatcher)

        dispatcher.dispatch(event("/audio hello"))
        assertEquals(0, handler.calls)

        dispatcher.dispatch(event("question"))
        assertEquals(1, handler.calls)
    }

    private class Handler {
        var calls = 0

        @Listener
        @Filter(targets = [Filter.Targets(mentionBot = true)])
        @MultiFilter(
            value = [Filter(value = "/audio", matchType = MatchType.STARTS_WITH)],
            type = MultiFilter.Type.NONE,
        )
        fun handle(event: GroupMessageEvent) {
            calls++
        }
    }

    private fun event(text: String) = GroupMessageEvent(
        time = 0,
        selfId = 9,
        peerId = 123,
        messageSeq = 1,
        senderId = 1,
        segments = listOf(TextSegment.of(text), MentionSegment.of(9)),
        group = GroupEntity(123, "test", 2, 10),
        groupMember = GroupMemberEntity(1, "", Sex.UNKNOWN, 123, "", "", 0, Role.MEMBER, 0, 0, null),
    )
}

