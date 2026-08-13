package indi.kyson.laocai.bot.core.segment

import tools.jackson.databind.JsonNode
import tools.jackson.databind.json.JsonMapper
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class SegmentTest {

    private val jsonMapper = JsonMapper.builder().build()

    @Test
    fun textSegmentSerializesToTypeAndDataShape() {
        val segment: Segment = TextSegment.of("hi")

        val tree = jsonMapper.valueToTree<JsonNode>(segment)

        assertEquals("text", tree.get("type").asString())
        assertEquals("hi", tree.get("data").get("text").asString())
        assertEquals(2, tree.size())
    }

    @Test
    fun textSegmentRoundTripsThroughDeserialization() {
        val segment = jsonMapper.readValue("{\"type\":\"text\",\"data\":{\"text\":\"hi\"}}", Segment::class.java)

        assertIs<TextSegment>(segment)
        assertEquals("hi", segment.text)
    }

    @Test
    fun unknownTypeFallsBackToUnknownSegmentInsteadOfThrowing() {
        val segment = jsonMapper.readValue("{\"type\":\"foobar\",\"data\":{\"x\":1}}", Segment::class.java)

        assertIs<UnknownSegment>(segment)
        assertEquals("foobar", segment.type)
    }
}
