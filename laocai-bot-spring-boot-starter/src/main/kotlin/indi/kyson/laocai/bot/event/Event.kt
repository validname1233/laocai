package indi.kyson.laocai.bot.event

import indi.kyson.laocai.bot.entity.FriendEntity
import indi.kyson.laocai.bot.entity.GroupEntity
import indi.kyson.laocai.bot.entity.GroupMemberEntity
import indi.kyson.laocai.bot.enums.MessageScene
import indi.kyson.laocai.bot.segment.Segment
import com.fasterxml.jackson.annotation.JsonProperty
import org.slf4j.LoggerFactory
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

        private val log = LoggerFactory.getLogger(javaClass)

        private data class GroupMessageData(
            @JsonProperty("message_scene")
            val messageScene: String,
            @JsonProperty("peer_id")
            val peerId: Long,
            @JsonProperty("message_seq")
            val messageSeq: Long,
            @JsonProperty("sender_id")
            val senderId: Long,
            val time: Long,
            val segments: List<Segment>,
            val group: GroupEntity,
            @JsonProperty("group_member")
            val groupMember: GroupMemberEntity,
        )

        private data class FriendMessageData(
            @JsonProperty("message_scene")
            val messageScene: String,
            @JsonProperty("peer_id")
            val peerId: Long,
            @JsonProperty("message_seq")
            val messageSeq: Long,
            @JsonProperty("sender_id")
            val senderId: Long,
            val time: Long,
            val segments: List<Segment>,
            val friend: FriendEntity,
        )

        private data class MessageRecallData(
            @JsonProperty("message_scene")
            val messageScene: String,
            @JsonProperty("peer_id")
            val peerId: Long,
            @JsonProperty("message_seq")
            val messageSeq: Long,
            @JsonProperty("sender_id")
            val senderId: Long,
            @JsonProperty("operator_id")
            val operatorId: Long,
            @JsonProperty("display_suffix")
            val displaySuffix: String,
        )

        override fun deserialize(p: JsonParser, context: DeserializationContext): Event {
            val root: JsonNode = p.readValueAsTree()

            val eventType = root.get("event_type").asString()
            val time = root.get("time").asLong()
            val selfId = root.get("self_id").asLong()
            val dataNode = root.get("data")

            // log.info("data: {}", dataNode.toString())

            return when (eventType) {
                "bot_offline" -> BotOfflineEvent(time, selfId, dataNode.get("reason").asString())
                "message_recall" -> {
                    val d = context.readTreeAsValue(dataNode, MessageRecallData::class.java)
                    MessageRecallEvent(
                        time,
                        selfId,
                        MessageScene.fromValue(d.messageScene),
                        d.peerId,
                        d.messageSeq,
                        d.senderId,
                        d.operatorId,
                        d.displaySuffix,
                    )
                }
                "message_receive" -> {
                    when (val messageScene = dataNode.get("message_scene").asString()) {
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
