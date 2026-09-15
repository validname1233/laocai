package indi.kyson.laocai.bot.segment

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * @提及消息段。
 */
class MentionSegment private constructor(override val data: Data) : Segment {

    data class Data(@JsonProperty("user_id") val userId: Long) {
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
