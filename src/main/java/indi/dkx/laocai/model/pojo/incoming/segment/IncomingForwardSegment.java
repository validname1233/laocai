package indi.dkx.laocai.model.pojo.incoming.segment;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * forward 合并转发消息段
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class IncomingForwardSegment extends IncomingSegment {
    /**
     * 合并转发消息ID
     */
    private String forwardId;
}
