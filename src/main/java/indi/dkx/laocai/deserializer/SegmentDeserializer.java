package indi.dkx.laocai.deserializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import indi.dkx.laocai.model.pojo.segment.Segment;
import indi.dkx.laocai.model.pojo.data.TextSegmentData;
import indi.dkx.laocai.model.pojo.data.ReplySegmentData;
import indi.dkx.laocai.model.pojo.data.MentionSegmentData;
import indi.dkx.laocai.model.pojo.data.FaceSegmentData;
import indi.dkx.laocai.model.pojo.segment.TextSegment;
import indi.dkx.laocai.model.pojo.segment.ReplySegment;
import indi.dkx.laocai.model.pojo.segment.MentionSegment;
import indi.dkx.laocai.model.pojo.segment.FaceSegment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class SegmentDeserializer extends JsonDeserializer<Segment<?>> {
    private final ObjectMapper mapper;
    //private static final Logger log = LoggerFactory.getLogger(SegmentDeserializer.class);

    @Override
    public Segment<?> deserialize(JsonParser p, DeserializationContext context) throws IOException {

        //log.debug("进入 Deserializer.deserialize()");
        //调试用日志信息

        JsonNode root = p.getCodec().readTree(p);
        String type = root.get("type").asText();
        JsonNode dataNode = root.get("data");
        return switch(type){
            case "text" ->{
                TextSegmentData data = mapper.treeToValue(dataNode, TextSegmentData.class);
                var Segment = new TextSegment(data.text());
                yield Segment;
            }
            case "mention" ->{
                MentionSegmentData data = mapper.treeToValue(dataNode, MentionSegmentData.class);
                var Segment = new MentionSegment(data.user_id());
                yield Segment;
            }
            case "reply" ->{
                ReplySegmentData data = mapper.treeToValue(dataNode, ReplySegmentData.class);
                var Segment = new ReplySegment(data.messageSeq());
                yield Segment;
            }
            case "face" ->{
                FaceSegmentData data = mapper.treeToValue(dataNode, FaceSegmentData.class);
                var Segment = new FaceSegment(data.faceId(), data.isLarge());
                yield Segment;
            }
            default -> throw new IllegalArgumentException("Unknown type: " + type);
        };
        
    }
}