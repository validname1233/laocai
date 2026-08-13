package indi.kyson.laocai.bot.core.event

import indi.kyson.laocai.bot.core.entity.FriendEntity
import indi.kyson.laocai.bot.core.segment.Segment

/**
 * 好友消息事件。
 *
 * 好友消息比公共基类多了好友关系信息，便于在处理器里直接读取展示字段。
 */
data class FriendMessageEvent(
    override val time: Long,
    override val selfId: Long,
    override val peerId: Long,
    override val messageSeq: Long,
    override val senderId: Long,
    override val segments: List<Segment>,
    val friend: FriendEntity,
) : MessageEvent
