package indi.kyson.laocai.app.conversation

import indi.kyson.laocai.app.history.ChatHistory
import indi.kyson.laocai.bot.enums.ImageSubType
import indi.kyson.laocai.bot.segment.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.util.TimeZone

class ChatHistoryRendererTest {
    private val renderer = ChatHistoryRenderer()

    @Test
    fun rendersNamesAndMentionsWithoutChangingSegmentOrder() {
        val history = ChatHistory(0, 1001, listOf(
            TextSegment.of("hello\r\nworld\n"), MentionSegment.of(1002),
            image("A"), TextSegment.of("!"), MentionSegment.of(1003),
        ))
        val result = renderer.render(listOf(history), mapOf(1001L to "小明", 1002L to "小红"), setOf("A"))
        assertEquals("[1970-01-01 08:00:00] senderId=1001 senderName=小明\nhello world @小红（userId: 1002）[图片#1]!@1003（userId: 1003）", result.text)
        assertEquals(listOf("A"), result.imageResources)
    }

    @ParameterizedTest
    @ValueSource(strings = ["A", "B", "C"])
    fun missingImageDoesNotConsumeGlobalNumber(missing: String) {
        val histories = listOf(
            ChatHistory(10, 1001, listOf(image("A"), image("B"))),
            ChatHistory(0, 1002, listOf(image("C"))),
        )
        val expectedBodies = when (missing) {
            "A" -> listOf("[图片（资源不可用）][图片#1]", "[图片#2]")
            "B" -> listOf("[图片#1][图片（资源不可用）]", "[图片#2]")
            else -> listOf("[图片#1][图片#2]", "[图片（资源不可用）]")
        }
        val result = renderer.render(histories, emptyMap(), linkedSetOf("C", "B", "A") - missing)
        assertEquals("[1970-01-01 08:00:10] senderId=1001\n${expectedBodies[0]}\n\n[1970-01-01 08:00:00] senderId=1002\n${expectedBodies[1]}", result.text)
        assertEquals(listOf("A", "B", "C") - missing, result.imageResources)
    }

    @Test
    fun repeatedResourceKeepsEachOccurrenceAndOutgoingImageUsesUri() {
        val histories = listOf(ChatHistory(0, 1, listOf(
            image("A"), OutgoingImageSegment.of("file:///picture.png", ImageSubType.NORMAL, null), image("A"),
        )))
        val result = renderer.render(histories, emptyMap(), setOf("A", "file:///picture.png"))
        assertEquals("[1970-01-01 08:00:00] senderId=1\n[图片#1][图片#2][图片#3]", result.text)
        assertEquals(listOf("A", "file:///picture.png", "A"), result.imageResources)
    }

    @Test
    fun omitsMissingSenderNameButRetainsUnavailableOnlyMessage() {
        val result = renderer.render(listOf(ChatHistory(0, 1001, listOf(image("missing")))), mapOf(1001L to "  "), emptySet())
        assertEquals("[1970-01-01 08:00:00] senderId=1001\n[图片（资源不可用）]", result.text)
        assertEquals(emptyList<String>(), result.imageResources)
    }

    @Test
    fun skipsEmptyAndUnsupportedMessagesWithoutRenderingNestedImages() {
        val histories = listOf(
            ChatHistory(0, 1, emptyList()), ChatHistory(0, 2, listOf(TextSegment.of(""))),
            ChatHistory(0, 3, listOf(FaceSegment.of("1", false), OutgoingRecordSegment.of("secret"))),
            ChatHistory(0, 4, listOf(IncomingReplySegment.Data(1, 2, "name", 0, listOf(image("A"))).toSegment())),
        )
        assertEquals(RenderedHistory("", emptyList()), renderer.render(histories, emptyMap(), setOf("A")))
        assertEquals(RenderedHistory("", emptyList()), renderer.render(emptyList(), emptyMap(), emptySet()))
    }

    @Test
    fun renderingDoesNotDependOnDefaultTimeZone() {
        val original = TimeZone.getDefault()
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("America/New_York"))
            val result = renderer.render(listOf(ChatHistory(0, 1, listOf(TextSegment.of("text")))), emptyMap(), emptySet())
            assertEquals("[1970-01-01 08:00:00] senderId=1\ntext", result.text)
        } finally {
            TimeZone.setDefault(original)
        }
    }

    private fun image(id: String) = IncomingImageSegment.Data(id, "", 1, 1, "", ImageSubType.NORMAL).toSegment()
}
