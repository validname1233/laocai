package indi.dkx.laocai.model.pojo.segment;

import lombok.Data;
import lombok.EqualsAndHashCode;
import indi.dkx.laocai.model.pojo.data.FaceSegmentData;

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
