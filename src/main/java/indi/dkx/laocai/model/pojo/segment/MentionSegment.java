package indi.dkx.laocai.model.pojo.segment;

import lombok.Data;
import lombok.EqualsAndHashCode;
import indi.dkx.laocai.model.pojo.data.MentionSegmentData;

/**
 * mention 提及消息段
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class MentionSegment extends Segment<MentionSegmentData> {
    public MentionSegment (Long userId){
        super("mention", new MentionSegmentData(userId));
    }
}
