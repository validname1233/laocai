package indi.kyson.laocai.bot.event

import indi.kyson.laocai.bot.entity.GroupEntity
import indi.kyson.laocai.bot.entity.GroupMemberEntity
import indi.kyson.laocai.bot.segment.Segment

/**
 * 群消息事件。
 *
 * 群消息处理通常同时需要群信息和成员信息，放在同一个对象里更方便直接使用。
 */
data class GroupMessageEvent(
    override val time: Long,
    override val selfId: Long,
    override val peerId: Long,
    override val messageSeq: Long,
    override val senderId: Long,
    override val segments: List<Segment>,
    val group: GroupEntity,
    val groupMember: GroupMemberEntity,
) : MessageEvent
