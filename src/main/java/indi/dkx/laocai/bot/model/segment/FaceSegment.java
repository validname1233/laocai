package indi.dkx.laocai.bot.model.segment;

import lombok.Data;
import lombok.EqualsAndHashCode;
import indi.dkx.laocai.bot.model.segment.data.FaceSegmentData;

/**
 * face 表情消息段
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class FaceSegment extends Segment<FaceSegmentData> {
    public FaceSegment(String faceId, boolean isLarge){
        super("face", new FaceSegmentData(faceId, isLarge));
    }
}
