package indi.dkx.laocai.bot.model.segment;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import indi.dkx.laocai.bot.model.segment.data.*;

@JsonDeserialize(using = Segment.SegmentDeserializer.class)
@Data
public class Segment {
    private final String type;
    private final SegmentData data;
    Segment(String type, SegmentData data) {
        this.type = type;
        this.data = data;
    }

    @Component
    @RequiredArgsConstructor
    public static class SegmentDeserializer extends JsonDeserializer<Segment> {
        private final ObjectMapper mapper;
        
        //private static final Logger log = LoggerFactory.getLogger(SegmentDeserializer.class);
        //调试用日志
        
        @Override
        public Segment deserialize(JsonParser p, DeserializationContext context) throws IOException {

            //log.debug("进入 Deserializer.deserialize()");
            //调试用日志信息

            JsonNode root = p.getCodec().readTree(p);
            String type = root.get("type").asText();
            JsonNode dataNode = root.get("data");
            SegmentData data = switch (type) {
                case "text" -> mapper.treeToValue(dataNode, TextSegmentData.class);
                case "mention" -> mapper.treeToValue(dataNode, MentionSegmentData.class);
                case "reply" -> mapper.treeToValue(dataNode, ReplySegmentData.class);
                case "face" -> mapper.treeToValue(dataNode, FaceSegmentData.class);
                default -> throw new IllegalArgumentException("Unknown type: " + type);
            };
            return new Segment(type, data);
        }
    }
}
