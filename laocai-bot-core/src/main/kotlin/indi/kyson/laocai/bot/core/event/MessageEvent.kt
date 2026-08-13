package indi.kyson.laocai.bot.core.event

import indi.kyson.laocai.bot.core.segment.IncomingImageSegment
import indi.kyson.laocai.bot.core.segment.MentionSegment
import indi.kyson.laocai.bot.core.segment.Segment
import indi.kyson.laocai.bot.core.segment.TextSegment

/**
 * 消息事件公共基类。
 *
 * 群消息、好友消息虽然载体不同，但它们都共享一组消息字段和段落解析能力。
 */
sealed interface MessageEvent : Event {

    val peerId: Long

    val messageSeq: Long

    val senderId: Long

    val segments: List<Segment>

    /**
     * 提取纯文本内容。
     */
    val plainText: String
        get() = segments.filterIsInstance<TextSegment>().joinToString("") { it.text }

    /**
     * 提取图片临时地址。
     */
    val imageUrls: Array<String>
        get() = segments.filterIsInstance<IncomingImageSegment>().map { it.tempUrl }.toTypedArray()

    /**
     * 提取被 @ 的用户 ID。
     */
    val mentionedUserIds: LongArray
        get() = segments.filterIsInstance<MentionSegment>().map { it.userId }.toLongArray()
}
