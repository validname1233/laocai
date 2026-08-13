package indi.kyson.laocai.bot.core.segment

import tools.jackson.core.JsonParser
import tools.jackson.databind.DeserializationContext
import tools.jackson.databind.JsonNode
import tools.jackson.databind.ValueDeserializer
import tools.jackson.databind.annotation.JsonDeserialize

/**
 * 消息段统一模型。
 *
 * 消息内容在协议里是按 segment 切分的，每种类型对应一个独立实现类，
 * 类型判断和构造都收敛到各自的类里，而不是依赖外部的 data 类型判断链。
 */
@JsonDeserialize(using = Segment.SegmentDeserializer::class)
sealed interface Segment {

    val type: String

    val data: Any

    class SegmentDeserializer : ValueDeserializer<Segment>() {

        override fun deserialize(p: JsonParser, context: DeserializationContext): Segment {
            val root: JsonNode = p.readValueAsTree()
            val type = root.get("type").asString()
            val dataNode = root.get("data")
            return when (type) {
                "text" -> context.readTreeAsValue(dataNode, TextSegment.Data::class.java).toSegment()
                "mention" -> context.readTreeAsValue(dataNode, MentionSegment.Data::class.java).toSegment()
                "face" -> context.readTreeAsValue(dataNode, FaceSegment.Data::class.java).toSegment()
                "image" -> context.readTreeAsValue(dataNode, IncomingImageSegment.Data::class.java).toSegment()
                "reply" -> context.readTreeAsValue(dataNode, IncomingReplySegment.Data::class.java).toSegment()
                else -> UnknownSegment.of(type, dataNode)
            }
        }
    }
}
