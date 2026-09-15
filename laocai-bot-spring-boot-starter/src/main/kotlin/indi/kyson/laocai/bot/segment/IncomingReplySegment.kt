package indi.kyson.laocai.bot.segment

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * 入站的引用回复消息段。
 *
 * 只在反序列化时产生，携带被回复消息的完整内容。
 */
class IncomingReplySegment private constructor(override val data: Data) : Segment {

    data class Data(
        @JsonProperty("message_seq")
        val messageSeq: Long,
        @JsonProperty("sender_id")
        val senderId: Long,
        @JsonProperty("sender_name")
        val senderName: String?,
        val time: Long,
        val segments: List<Segment>,
    ) {
        fun toSegment(): IncomingReplySegment = IncomingReplySegment(this)
    }

    override val type: String
        get() = "reply"

    @get:JsonIgnore
    val messageSeq: Long
        get() = data.messageSeq

    @get:JsonIgnore
    val senderId: Long
        get() = data.senderId

    @get:JsonIgnore
    val senderName: String?
        get() = data.senderName

    @get:JsonIgnore
    val time: Long
        get() = data.time

    @get:JsonIgnore
    val segments: List<Segment>
        get() = data.segments
}
