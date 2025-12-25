package indi.dkx.laocai.model.pojo.incoming.segment;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * face 表情消息段
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class IncomingFaceSegment extends IncomingSegment {
    /**
     * 表情ID
     */
    private String faceId;
}
