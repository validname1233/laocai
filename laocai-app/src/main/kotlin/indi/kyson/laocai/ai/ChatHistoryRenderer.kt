package indi.kyson.laocai.ai

import indi.kyson.laocai.ai.model.ChatHistory
import indi.kyson.laocai.ai.model.RenderedHistory
import indi.kyson.laocai.bot.segment.IncomingImageSegment
import indi.kyson.laocai.bot.segment.MentionSegment
import indi.kyson.laocai.bot.segment.OutgoingImageSegment
import indi.kyson.laocai.bot.segment.Segment
import indi.kyson.laocai.bot.segment.TextSegment
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/** Pure conversion from stored segments to the AI history representation. */
@Component
class ChatHistoryRenderer {
    /**
     * 
     */
    fun render(
        histories: List<ChatHistory>,
        displayNames: Map<Long, String>,
        availableImageIds: Set<String>,
    ): RenderedHistory {
        val imageResources = mutableListOf<String>()
        val renderedMessages = histories.mapNotNull { history ->
            val body = buildString {
                history.segments.forEach { segment ->
                    when (segment) {
                        is TextSegment -> append(segment.text.replace(Regex("\\R"), " "))
                        is MentionSegment -> {
                            val name = displayNames[segment.userId]?.takeIf { it.isNotBlank() } ?: segment.userId.toString()
                            append("@$name（userId: ${segment.userId}）")
                        }
                        is IncomingImageSegment -> appendImage(referenceOf(segment), availableImageIds, imageResources)
                        is OutgoingImageSegment -> appendImage(referenceOf(segment), availableImageIds, imageResources)
                        else -> Unit
                    }
                }
            }
            if (body.isEmpty()) return@mapNotNull null
            val senderName = displayNames[history.senderId]?.takeIf { it.isNotBlank() }
            val header = buildString {
                append('[').append(TIME_FORMATTER.format(Instant.ofEpochSecond(history.time))).append("] senderId=")
                    .append(history.senderId)
                if (senderName != null) append(" senderName=").append(senderName)
            }
            "$header\n$body"
        }
        return RenderedHistory(renderedMessages.joinToString("\n\n"), imageResources)
    }

    private fun StringBuilder.appendImage(
        reference: String?,
        availableImageIds: Set<String>,
        imageResources: MutableList<String>,
    ) {
        if (reference != null && reference in availableImageIds) {
            imageResources += reference
            append("[图片#").append(imageResources.size).append(']')
        } else {
            append("[图片（资源不可用）]")
        }
    }

    companion object {
        private val TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.of("Asia/Shanghai"))

        /** Shared reference derivation: inbound images use resource_id, outbound images use uri. */
        @JvmStatic
        fun referenceOf(segment: Segment): String? = when (segment) {
            is IncomingImageSegment -> segment.resourceId.takeIf(String::isNotBlank)
            is OutgoingImageSegment -> segment.uri.takeIf(String::isNotBlank)
            else -> null
        }
    }
}
