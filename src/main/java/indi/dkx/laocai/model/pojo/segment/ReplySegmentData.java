package indi.dkx.laocai.model.pojo.segment;

/**
 * reply 回复消息段 data
 * @param messageSeq 被引用的消息序列号
 */
public record ReplySegmentData(
        Long messageSeq
) {
}
