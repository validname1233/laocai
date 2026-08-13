package indi.kyson.laocai.bot.core.event

import indi.kyson.laocai.bot.core.entity.FriendEntity
import indi.kyson.laocai.bot.core.entity.GroupEntity
import indi.kyson.laocai.bot.core.entity.GroupMemberEntity
import indi.kyson.laocai.bot.core.segment.Segment
import tools.jackson.core.JsonParser
import tools.jackson.databind.DeserializationContext
import tools.jackson.databind.JsonNode
import tools.jackson.databind.ValueDeserializer
import tools.jackson.databind.annotation.JsonDeserialize

/**
 * 统一事件载体。
 *
 * 事件处理链按具体子类型直接分发，不再需要反射掏泛型实参。
 */
@JsonDeserialize(using = Event.EventDeserializer::class)
sealed interface Event {

    val time: Long

    val selfId: Long

    class EventDeserializer : ValueDeserializer<Event>() {

        private data class GroupMessageData(
            val messageScene: String,
            val peerId: Long,
            val messageSeq: Long,
            val senderId: Long,
            val time: Long,
            val segments: List<Segment>,
            val group: GroupEntity,
            val groupMember: GroupMemberEntity,
        )

        private data class FriendMessageData(
            val messageScene: String,
            val peerId: Long,
            val messageSeq: Long,
            val senderId: Long,
            val time: Long,
            val segments: List<Segment>,
            val friend: FriendEntity,
        )

        override fun deserialize(p: JsonParser, context: DeserializationContext): Event {
            val root: JsonNode = p.readValueAsTree()

            val eventType = root.get("event_type").asString()
            val time = root.get("time").asLong()
            val selfId = root.get("self_id").asLong()
            val dataNode = root.get("data")

            return when (eventType) {
                "message_receive" -> {
                    val messageScene = dataNode.get("message_scene").asString()
                    when (messageScene) {
                        "friend" -> {
                            val d = context.readTreeAsValue(dataNode, FriendMessageData::class.java)
                            FriendMessageEvent(time, selfId, d.peerId, d.messageSeq, d.senderId, d.segments, d.friend)
                        }
                        "group" -> {
                            val d = context.readTreeAsValue(dataNode, GroupMessageData::class.java)
                            GroupMessageEvent(
                                time, selfId, d.peerId, d.messageSeq, d.senderId, d.segments, d.group, d.groupMember,
                            )
                        }
                        else -> throw IllegalArgumentException("Unknown message_scene: $messageScene")
                    }
                }
                else -> throw IllegalArgumentException("Unknown event_type: $eventType")
            }
        }
    }
}
