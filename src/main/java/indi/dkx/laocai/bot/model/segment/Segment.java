package indi.dkx.laocai.bot.model.segment;

import lombok.Data;

import lombok.extern.slf4j.Slf4j;

import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.annotation.JsonDeserialize;
import indi.dkx.laocai.bot.model.segment.data.*;

/**
 * 消息段统一模型。
 * <p>
 * 消息内容在协议里是按 segment 切分的，统一包一层才能在不同类型之间切换解析实现。
 */
@JsonDeserialize(using = Segment.SegmentDeserializer.class)
@Data
public class Segment {
    private final String type;
    private final SegmentData data;
    Segment(String type, SegmentData data) {
        this.type = type;
        this.data = data;
    }

    @Slf4j
    public static class SegmentDeserializer extends ValueDeserializer<Segment> {

        @Override
        public Segment deserialize(JsonParser p, DeserializationContext context) {

            log.debug("进入 Deserializer.deserialize()");

            JsonNode root = p.readValueAsTree();
            String type = root.get("type").asString();
            JsonNode dataNode = root.get("data");
            SegmentData data = switch (type) {
                case "text" -> context.readTreeAsValue(dataNode, TextSegmentData.class);
                case "mention" -> context.readTreeAsValue(dataNode, MentionSegmentData.class);
                case "face" -> context.readTreeAsValue(dataNode, FaceSegmentData.class);
                case "image" -> context.readTreeAsValue(dataNode, IncomingImageSegmentData.class);
                case "reply" -> context.readTreeAsValue(dataNode, IncomingReplySegmentData.class);
                default -> throw new IllegalArgumentException("Unknown type: " + type);
            };
            return new Segment(type, data);
        }
    }
}


