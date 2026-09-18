package indi.kyson.laocai.bot.event

import indi.kyson.laocai.bot.enums.MessageScene

import tools.jackson.databind.json.JsonMapper
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class EventDeserializerTest {

    private val jsonMapper = JsonMapper.builder().build()

    @Test
    fun botOfflineEventDeserializesThroughEvent() {
        val json = """
            {
                "event_type": "bot_offline",
                "time": 2200000000,
                "self_id": 4000000000,
                "data": {
                    "reason": "账号在其他设备登录"
                }
            }
        """.trimIndent()

        val event = jsonMapper.readValue(json, Event::class.java)

        assertIs<BotOfflineEvent>(event)
        assertEquals(2200000000L, event.time)
        assertEquals(4000000000L, event.selfId)
        assertEquals("账号在其他设备登录", event.reason)
    }

    @Test
    fun botOfflineEventPreservesEmptyReason() {
        val json = """
            {
                "event_type": "bot_offline",
                "time": 0,
                "self_id": 1,
                "data": {
                    "reason": ""
                }
            }
        """.trimIndent()

        val event = jsonMapper.readValue(json, Event::class.java)

        assertIs<BotOfflineEvent>(event)
        assertEquals("", event.reason)
    }
    @Test
    fun messageRecallEventDeserializesThroughEvent() {
        val json = """
            {
                "event_type": "message_recall",
                "time": 2200000000,
                "self_id": 4000000000,
                "data": {
                    "message_scene": "group",
                    "peer_id": 634550174,
                    "message_seq": 35949,
                    "sender_id": 4017491129,
                    "operator_id": 2331630699,
                    "display_suffix": "admin withdrew"
                }
            }
        """.trimIndent()

        val event = jsonMapper.readValue(json, Event::class.java)

        assertIs<MessageRecallEvent>(event)
        assertEquals(2200000000L, event.time)
        assertEquals(4000000000L, event.selfId)
        assertEquals(MessageScene.GROUP, event.messageScene)
        assertEquals(634550174L, event.peerId)
        assertEquals(35949L, event.messageSeq)
        assertEquals(4017491129L, event.senderId)
        assertEquals(2331630699L, event.operatorId)
        assertEquals("admin withdrew", event.displaySuffix)
    }
}
