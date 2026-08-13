package indi.kyson.laocai.bot.core.listener

import indi.kyson.laocai.bot.core.entity.GroupEntity
import indi.kyson.laocai.bot.core.entity.GroupMemberEntity
import indi.kyson.laocai.bot.core.enums.Role
import indi.kyson.laocai.bot.core.enums.Sex
import indi.kyson.laocai.bot.core.event.Event
import indi.kyson.laocai.bot.core.event.GroupMessageEvent
import reactor.core.publisher.Flux
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EventDispatcherConsumeTest {

    private class RecordingListener {
        val received = CopyOnWriteArrayList<Long>()

        fun onEvent(event: Event) {
            received.add((event as GroupMessageEvent).messageSeq)
            if (event.messageSeq == 1L) {
                // 让第一个事件的处理阻塞住，逼出背压：后续事件在缓冲区里堆积直至溢出。
                Thread.sleep(400)
            }
        }
    }

    private fun sampleEvent(seq: Long) = GroupMessageEvent(
        time = 0,
        selfId = 1,
        peerId = 100,
        messageSeq = seq,
        senderId = 200,
        segments = emptyList(),
        group = GroupEntity(100, "group", 1, 100),
        groupMember = GroupMemberEntity(200, "nick", Sex.UNKNOWN, 100, "", "", 1, Role.MEMBER, 0, 0, null),
    )

    private fun registerRecordingListener(dispatcher: EventDispatcher): RecordingListener {
        val recorder = RecordingListener()
        val method = RecordingListener::class.java.getDeclaredMethod("onEvent", Event::class.java)
        dispatcher.register(EventListener(recorder, method) { true })
        return recorder
    }

    private fun awaitUntil(timeoutMs: Long = 2000, condition: () -> Boolean) {
        val deadline = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < deadline) {
            if (condition()) return
            Thread.sleep(20)
        }
        assertTrue(condition(), "condition not met within ${timeoutMs}ms")
    }

    @Test
    fun consumeDispatchesEachEventToRegisteredListeners() {
        val dispatcher = EventDispatcher()
        val recorder = registerRecordingListener(dispatcher)
        val events = Flux.just<Event>(sampleEvent(2), sampleEvent(3), sampleEvent(4))

        val disposable = dispatcher.consume(events, concurrency = 4, bufferSize = 16)

        awaitUntil { recorder.received.size == 3 }
        assertEquals(setOf(2L, 3L, 4L), recorder.received.toSet())
        disposable.dispose()
    }

    @Test
    fun consumeDropsLatestInsteadOfUnboundedBufferingWhenConsumerIsSlow() {
        val dispatcher = EventDispatcher()
        val recorder = registerRecordingListener(dispatcher)
        // 单并发、极小缓冲区：第一个事件阻塞处理 400ms 期间，其余事件全部瞬时到达。
        val events = Flux.fromIterable<Event>((1L..20L).map { sampleEvent(it) })

        val disposable = dispatcher.consume(events, concurrency = 1, bufferSize = 2)

        awaitUntil(timeoutMs = 3000) { recorder.received.size in 1 until 20 }
        assertTrue(recorder.received.contains(1L), "首个事件应该被处理到")
        assertTrue(recorder.received.size < 20, "缓冲区溢出时应丢弃最新事件，而不是全部处理或无限堆积")
        disposable.dispose()
    }
}
