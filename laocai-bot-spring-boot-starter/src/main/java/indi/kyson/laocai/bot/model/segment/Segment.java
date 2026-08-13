package indi.kyson.laocai.bot.model.segment;

import lombok.extern.slf4j.Slf4j;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.annotation.JsonDeserialize;

/**
 * 消息段统一模型。
 * <p>
 * 消息内容在协议里是按 segment 切分的，每种类型对应一个独立实现类，
 * 类型判断和构造都收敛到各自的类里，而不是依赖外部的 data 类型判断链。
 */
@JsonDeserialize(using = Segment.SegmentDeserializer.class)
public sealed interface Segment
        permits TextSegment, MentionSegment, FaceSegment,
                IncomingReplySegment, OutgoingReplySegment,
                IncomingImageSegment, OutgoingImageSegment,
                UnknownSegment, OutgoingRecordSegment {

    String getType();

    Object getData();

    @Slf4j
    final class SegmentDeserializer extends ValueDeserializer<Segment> {

        @Override
        public Segment deserialize(JsonParser p, DeserializationContext context) {

            log.debug("进入 Deserializer.deserialize()");

            JsonNode root = p.readValueAsTree();
            String type = root.get("type").asString();
            JsonNode dataNode = root.get("data");
            return switch (type) {
                case "text" -> context.readTreeAsValue(dataNode, TextSegment.Data.class).toSegment();
                case "mention" -> context.readTreeAsValue(dataNode, MentionSegment.Data.class).toSegment();
                case "face" -> context.readTreeAsValue(dataNode, FaceSegment.Data.class).toSegment();
                case "image" -> context.readTreeAsValue(dataNode, IncomingImageSegment.Data.class).toSegment();
                case "reply" -> context.readTreeAsValue(dataNode, IncomingReplySegment.Data.class).toSegment();
                default -> UnknownSegment.of(type, dataNode);
            };
        }
    }
}
