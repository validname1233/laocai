package indi.dkx.laocai.bot.deserializer;

import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.JsonNode;
import indi.dkx.laocai.bot.model.event.Event;
import indi.dkx.laocai.bot.model.event.data.IncomingFriendMessage;
import indi.dkx.laocai.bot.model.event.data.IncomingGroupMessage;

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
                        // 3. 使用 p.objectReadContext() 代替注入的 mapper
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
