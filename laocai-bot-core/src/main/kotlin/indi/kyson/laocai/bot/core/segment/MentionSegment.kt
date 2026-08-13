package indi.kyson.laocai.bot.core.segment

import com.fasterxml.jackson.annotation.JsonIgnore

/**
 * @提及消息段。
 */
class MentionSegment private constructor(override val data: Data) : Segment {

    data class Data(val userId: Long) {
        fun toSegment(): MentionSegment = MentionSegment(this)
    }

    override val type: String
        get() = "mention"

    @get:JsonIgnore
    val userId: Long
        get() = data.userId

    companion object {
        @JvmStatic
        fun of(userId: Long): MentionSegment = MentionSegment(Data(userId))
    }
}
