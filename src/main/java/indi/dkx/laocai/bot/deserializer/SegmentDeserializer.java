package indi.dkx.laocai.bot.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import indi.dkx.laocai.bot.model.segment.Segment;
import indi.dkx.laocai.bot.model.segment.data.TextSegmentData;
import indi.dkx.laocai.bot.model.segment.data.ReplySegmentData;
import indi.dkx.laocai.bot.model.segment.data.MentionSegmentData;
import indi.dkx.laocai.bot.model.segment.data.FaceSegmentData;
import indi.dkx.laocai.bot.model.segment.TextSegment;
import indi.dkx.laocai.bot.model.segment.ReplySegment;
import indi.dkx.laocai.bot.model.segment.MentionSegment;
import indi.dkx.laocai.bot.model.segment.FaceSegment;
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
                yield new TextSegment(data.text());
            }
            case "mention" ->{
                MentionSegmentData data = mapper.treeToValue(dataNode, MentionSegmentData.class);
                yield new MentionSegment(data.user_id());
            }
            case "reply" ->{
                ReplySegmentData data = mapper.treeToValue(dataNode, ReplySegmentData.class);
                yield new ReplySegment(data.messageSeq());
            }
            case "face" ->{
                FaceSegmentData data = mapper.treeToValue(dataNode, FaceSegmentData.class);
                yield new FaceSegment(data.faceId(), data.isLarge());
            }
            default -> throw new IllegalArgumentException("Unknown type: " + type);
        };
        
    }
}