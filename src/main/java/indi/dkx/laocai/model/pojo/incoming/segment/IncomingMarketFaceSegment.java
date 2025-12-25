package indi.dkx.laocai.model.pojo.incoming.segment;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * market_face 市场表情消息段
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class IncomingMarketFaceSegment extends IncomingSegment {
    /**
     * 市场表情 URL
     */
    private String url;
}
