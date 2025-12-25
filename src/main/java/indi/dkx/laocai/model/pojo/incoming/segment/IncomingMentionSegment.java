package indi.dkx.laocai.model.pojo.incoming.segment;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * mention 提及消息段
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class IncomingMentionSegment extends IncomingSegment {
    private IncomingMentionSegmentData data;

    /**
     * 创建一个 mention 消息段
     * @param userId 用户 QQ号
     */
    public record IncomingMentionSegmentData(Long userId) {}

    public static IncomingMentionSegment of(Long userId) {
        IncomingMentionSegment segment = new IncomingMentionSegment();
        segment.setType("mention");
        segment.setData(new IncomingMentionSegmentData(userId));
        return segment;
    }
}
