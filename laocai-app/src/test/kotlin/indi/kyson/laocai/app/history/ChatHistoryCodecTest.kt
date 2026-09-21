package indi.kyson.laocai.app.history

import indi.kyson.laocai.bot.enums.ImageSubType
import indi.kyson.laocai.bot.segment.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import tools.jackson.databind.PropertyNamingStrategies
import tools.jackson.databind.json.JsonMapper

class ChatHistoryCodecTest {
    private val mapper = JsonMapper.builder().findAndAddModules()
        .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE).build()

    @Test
    fun recordRoundTripPreservesAllDeserializableSegmentTypesAndWireFields() {
        val segments = listOf(
            TextSegment.of("hello"), MarkdownSegment.of("**markdown**"), MentionSegment.of(1002),
            IncomingImageSegment.Data("rid", "https://example.test/image", 2, 3, "summary", ImageSubType.NORMAL).toSegment(),
            FaceSegment.of("1", false),
            IncomingReplySegment.Data(100, 1002, "sender", 0, listOf(MentionSegment.of(1003))).toSegment(),
        )
        val original = ChatHistory(123, 1001, segments)
        val json = mapper.writeValueAsString(original)
        val tree = mapper.readTree(json)
        assertEquals(setOf("time", "sender_id", "segments"), tree.propertyNames().toSet())
        assertEquals(mapper.readTree("""{"type":"mention","data":{"user_id":1002}}"""), mapper.readTree(mapper.writeValueAsString(segments[2])))
        val restored = mapper.readValue(json, ChatHistory::class.java)
        assertEquals(123L, restored.time)
        assertEquals(1001L, restored.senderId)
        assertEquals(segments.map { it.javaClass }, restored.segments.map { it.javaClass })
        assertEquals(tree, mapper.readTree(mapper.writeValueAsString(restored)))
    }

    @Test
    fun preservesUnknownSegmentsAtBothCollectionBoundaries() {
        val history = mapper.readValue("""{"time":0,"sender_id":1,"segments":[
            {"type":"text","data":{"text":"before"}},
            {"type":"future","data":{"secret":"must not reach AI"}},
            {"type":"mention","data":{"user_id":2}},
            {"type":"reply","data":{"message_seq":1,"sender_id":2,"sender_name":null,"time":0,"segments":[
                {"type":"future","data":{}}, {"type":"text","data":{"text":"nested"}}
            ]}}
        ]}""", ChatHistory::class.java)
        assertEquals(
            listOf(TextSegment::class.java, UnknownSegment::class.java, MentionSegment::class.java, IncomingReplySegment::class.java),
            history.segments.map { it.javaClass },
        )
        val topLevelUnknown = history.segments[1] as UnknownSegment
        assertEquals("future", topLevelUnknown.type)
        assertEquals("must not reach AI", topLevelUnknown.raw.get("secret").asString())

        val nested = (history.segments[3] as IncomingReplySegment).segments
        assertEquals(listOf(UnknownSegment::class.java, TextSegment::class.java), nested.map { it.javaClass })
        assertEquals("future", (nested[0] as UnknownSegment).type)
        assertEquals("nested", (nested[1] as TextSegment).text)
    }

    @Test
    fun segmentsMustBePresentAndNonNull() {
        listOf(
            """{"time":0,"sender_id":1,"content":"old","image_ids":[]}""",
            """{"time":0,"sender_id":1,"segments":null}""",
        ).forEach { json -> assertThrows(Exception::class.java) { mapper.readValue(json, ChatHistory::class.java) } }
    }
}
