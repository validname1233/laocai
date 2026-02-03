package indi.dkx.laocai.model.pojo.segment;

import indi.dkx.laocai.model.pojo.data.ReplySegmentData;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class ReplySegment extends Segment<ReplySegmentData> {
    public ReplySegment(Long messageSeq){
        super("reply", new ReplySegmentData(messageSeq));
    }
}
