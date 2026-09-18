package indi.kyson.laocai.bot.event

import indi.kyson.laocai.bot.enums.MessageScene

/**
 * 消息撤回事件。
 */
data class MessageRecallEvent(
    override val time: Long,
    override val selfId: Long,
    val messageScene: MessageScene,
    val peerId: Long,
    val messageSeq: Long,
    val senderId: Long,
    val operatorId: Long,
    val displaySuffix: String,
) : Event