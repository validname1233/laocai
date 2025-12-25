package indi.dkx.laocai.model.pojo.incoming.segment;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class IncomingReplySegment extends IncomingSegment {
    /**
     * 被引用的消息序列号
     */
    private Long messageSeq;
}
