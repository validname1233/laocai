package indi.kyson.laocai.bot.core.segment

import com.fasterxml.jackson.annotation.JsonIgnore

/**
 * 出站的引用回复消息段。
 *
 * 发送时只需要指明被回复消息的 messageSeq，协议字段名为 shouldReply。
 */
class OutgoingReplySegment private constructor(override val data: Data) : Segment {

    data class Data(val messageSeq: Long) {
        fun toSegment(): OutgoingReplySegment = OutgoingReplySegment(this)
    }

    override val type: String
        get() = "reply"

    @get:JsonIgnore
    val messageSeq: Long
        get() = data.messageSeq

    companion object {
        @JvmStatic
        fun of(messageSeq: Long): OutgoingReplySegment = OutgoingReplySegment(Data(messageSeq))
    }
}
