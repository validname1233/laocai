package indi.dkx.laocai.bot.model.segment;

import lombok.Data;
import lombok.EqualsAndHashCode;
import indi.dkx.laocai.bot.model.segment.data.MentionSegmentData;

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
