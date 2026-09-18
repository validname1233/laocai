package indi.kyson.laocai.bot.segment

import com.fasterxml.jackson.annotation.JsonIgnore

/**
 * 纯文本消息段。
 */
data class TextSegment private constructor(override val data: Data) : Segment {

    data class Data(val text: String) {
        fun toSegment(): TextSegment = TextSegment(this)
    }

    override val type: String
        get() = "text"

    @get:JsonIgnore
    val text: String
        get() = data.text

    companion object {
        @JvmStatic
        fun of(text: String): TextSegment = TextSegment(Data(text))
    }
}
