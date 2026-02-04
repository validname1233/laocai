package indi.dkx.laocai.bot.model.segment;

import indi.dkx.laocai.bot.model.segment.data.ReplySegmentData;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class ReplySegment extends Segment<ReplySegmentData> {
    public ReplySegment(Long messageSeq){
        super("reply", new ReplySegmentData(messageSeq));
    }
}
