package indi.dkx.laocai.model.pojo.segment;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * mention 提及消息段
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class MentionSegment extends Segment<MentionSegmentData> {
    public static MentionSegment of(Long userId) {
        MentionSegment segment = new MentionSegment();
        segment.setType("mention");
        segment.setData(new MentionSegmentData(userId));
        return segment;
    }
}
