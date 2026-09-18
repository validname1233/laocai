package indi.kyson.laocai.bot.segment

import com.fasterxml.jackson.annotation.JsonIgnore

/**
 * Markdown 消息段。
 */
data class MarkdownSegment private constructor(override val data: Data) : Segment {

    data class Data(val content: String) {
        fun toSegment(): MarkdownSegment = MarkdownSegment(this)
    }

    override val type: String
        get() = "markdown"

    @get:JsonIgnore
    val content: String
        get() = data.content

    companion object {
        @JvmStatic
        fun of(content: String): MarkdownSegment = MarkdownSegment(Data(content))
    }
}
