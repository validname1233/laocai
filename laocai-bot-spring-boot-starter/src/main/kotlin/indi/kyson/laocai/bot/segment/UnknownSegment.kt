package indi.kyson.laocai.bot.segment

import com.fasterxml.jackson.annotation.JsonIgnore
import tools.jackson.databind.JsonNode

/**
 * 未知类型的消息段。
 *
 * 协议新增或变更 segment 类型时不应让整条消息反序列化失败，先原样保留原始节点。
 */
class UnknownSegment private constructor(override val data: Data) : Segment {

    data class Data(val type: String, val raw: JsonNode) {
        fun toSegment(): UnknownSegment = UnknownSegment(this)
    }

    override val type: String
        get() = data.type

    @get:JsonIgnore
    val raw: JsonNode
        get() = data.raw

    companion object {
        internal fun of(type: String, raw: JsonNode): UnknownSegment = UnknownSegment(Data(type, raw))
    }
}
