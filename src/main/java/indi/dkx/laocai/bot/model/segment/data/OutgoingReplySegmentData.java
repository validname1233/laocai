package indi.dkx.laocai.bot.model.segment.data;

/**
 * 出站回复消息段。
 * <p>
 * 回复发送只需要原消息序号，协议会据此构造引用关系。
 * @param messageSeq 原消息序号
 */
public record OutgoingReplySegmentData(
    Long messageSeq
)implements SegmentData {
}


