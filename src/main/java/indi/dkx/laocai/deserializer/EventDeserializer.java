package indi.dkx.laocai.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import indi.dkx.laocai.model.pojo.event.Event;
import indi.dkx.laocai.model.pojo.message.IncomingFriendMessage;
import indi.dkx.laocai.model.pojo.message.IncomingGroupMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class EventDeserializer extends JsonDeserializer<Event<?>> {

    private final ObjectMapper mapper;

    @Override
    public Event<?> deserialize(JsonParser p, DeserializationContext context) throws IOException {
        JsonNode root = p.getCodec().readTree(p);

        String eventType = root.get("event_type").asText();
        JsonNode dataNode = root.get("data");

        // 公共字段
        Long time = root.get("time").asLong();
        Long selfId = root.get("self_id").asLong();

        return switch (eventType) {
            case "message_receive" -> {
                String messageScene = dataNode.get("message_scene").asText();
                yield switch (messageScene) {
                    case "friend" -> {
                        IncomingFriendMessage data = mapper.treeToValue(dataNode, IncomingFriendMessage.class);
                        yield new Event<>(eventType, time, selfId, data);
                    }
                    case "group" -> {
                        IncomingGroupMessage data = mapper.treeToValue(dataNode, IncomingGroupMessage.class);
                        yield new Event<>(eventType, time, selfId, data);
                    }
                    default -> throw new IllegalArgumentException("Unknown message_scene: " + messageScene);
                };
            }
            case "bot_offline" -> {
                // TODO: 处理 bot_offline 事件
                throw new IllegalArgumentException("Unknown event_type: " + eventType);
            }
            default -> throw new IllegalArgumentException("Unknown event_type: " + eventType);
        };
    }
}
