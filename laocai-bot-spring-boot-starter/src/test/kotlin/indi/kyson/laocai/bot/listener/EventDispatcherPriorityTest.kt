package indi.kyson.laocai.bot.listener

import indi.kyson.laocai.bot.entity.GroupEntity
import indi.kyson.laocai.bot.entity.GroupMemberEntity
import indi.kyson.laocai.bot.enums.Role
import indi.kyson.laocai.bot.enums.Sex
import indi.kyson.laocai.bot.event.Event
import indi.kyson.laocai.bot.event.GroupMessageEvent
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.test.Test
import kotlin.test.assertEquals

class EventDispatcherPriorityTest {

    private class RecordingTarget {
        val calls = CopyOnWriteArrayList<String>()

        fun first(event: Event) {
            calls.add("first")
        }

        fun second(event: Event) {
            calls.add("second")
        }

        fun third(event: Event) {
            calls.add("third")
        }
    }

    private class ThrowingTarget {
        fun throwException(event: Event) {
            error("expected test exception")
        }
    }

    private class RecordingSingleTarget {
        val calls = CopyOnWriteArrayList<String>()

        fun record(event: Event) {
            calls.add("recorded")
        }
    }

    @Test
    fun dispatchUsesAscendingPriorityAndStableRegistrationOrder() {
        val dispatcher = EventDispatcher()
        val target = RecordingTarget()
        val first = RecordingTarget::class.java.getDeclaredMethod("first", Event::class.java)
        val second = RecordingTarget::class.java.getDeclaredMethod("second", Event::class.java)
        val third = RecordingTarget::class.java.getDeclaredMethod("third", Event::class.java)

        dispatcher.register(EventListener(target, first, 0) { true })
        dispatcher.register(EventListener(target, second, -100) { true })
        dispatcher.register(EventListener(target, third, 0) { true })
        dispatcher.sortListeners()
        dispatcher.sortListeners()

        dispatcher.dispatch(sampleEvent())

        assertEquals(listOf("second", "first", "third"), target.calls.toList())
    }

    @Test
    fun exceptionDoesNotPreventLaterPriorityListeners() {
        val dispatcher = EventDispatcher()
        val throwingTarget = ThrowingTarget()
        val recordingTarget = RecordingSingleTarget()
        val throwingMethod = ThrowingTarget::class.java.getDeclaredMethod("throwException", Event::class.java)
        val recordingMethod = RecordingSingleTarget::class.java.getDeclaredMethod("record", Event::class.java)

        dispatcher.register(EventListener(throwingTarget, throwingMethod, -100) { true })
        dispatcher.register(EventListener(recordingTarget, recordingMethod, 0) { true })
        dispatcher.sortListeners()

        dispatcher.dispatch(sampleEvent())

        assertEquals(listOf("recorded"), recordingTarget.calls.toList())
    }

    private fun sampleEvent() = GroupMessageEvent(
        time = 0,
        selfId = 1,
        peerId = 100,
        messageSeq = 1,
        senderId = 200,
        segments = emptyList(),
        group = GroupEntity(100, "group", 1, 100),
        groupMember = GroupMemberEntity(200, "nick", Sex.UNKNOWN, 100, "", "", 1, Role.MEMBER, 0, 0, null),
    )
}