package indi.kyson.laocai.bot.deserializer;

import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.JsonNode;
import indi.kyson.laocai.bot.model.event.Event;
import indi.kyson.laocai.bot.model.event.data.IncomingFriendMessage;
import indi.kyson.laocai.bot.model.event.data.IncomingGroupMessage;

/**
 * 事件反序列化器。
 * <p>
 * 协议返回的是一层通用事件壳加一层具体 data，必须先读 eventType 再决定 data 的目标类型。
 */
public class EventDeserializer extends ValueDeserializer<Event<?>> {
    @Override
    public Event<?> deserialize(JsonParser p, DeserializationContext context) {

        JsonNode root = p.readValueAsTree();

        String eventType = root.get("event_type").asString();
        JsonNode dataNode = root.get("data");

        Long time = root.get("time").asLong();
        Long selfId = root.get("self_id").asLong();

        return switch (eventType) {
            case "message_receive" -> {
                String messageScene = dataNode.get("message_scene").asString();
                yield switch (messageScene) {
                    case "friend" -> {
                        IncomingFriendMessage data = context.readTreeAsValue(dataNode, IncomingFriendMessage.class);
                        yield new Event<>(eventType, time, selfId, data);
                    }
                    case "group" -> {
                        IncomingGroupMessage data = context.readTreeAsValue(dataNode, IncomingGroupMessage.class);
                        yield new Event<>(eventType, time, selfId, data);
                    }
                    default -> throw new IllegalArgumentException("Unknown message_scene: " + messageScene);
                };
            }
            case "bot_offline" -> throw new IllegalArgumentException("Unknown event_type: " + eventType);
            default -> throw new IllegalArgumentException("Unknown event_type: " + eventType);
        };
    }
}


