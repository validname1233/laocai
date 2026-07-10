package indi.dkx.laocai.bot.model.segment.data;

import indi.dkx.laocai.bot.model.segment.Segment;

import java.util.List;

/**
 * 入站回复消息段。
 * <p>
 * 回复消息本身也携带一段完整消息内容，处理时不能只看 messageSeq。
 * @param messageSeq 原消息序号
 * @param senderId 原消息发送者 ID
 * @param senderName 原消息发送者名称
 * @param time 原消息时间
 * @param segments 原消息内容段
 */
public record IncomingReplySegmentData(
        Long messageSeq,
        Long senderId,
        String senderName,
        Long time,
        List<Segment> segments
) implements SegmentData {
}


