package indi.dkx.laocai.model.pojo.incoming.segment;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * xml XML消息段
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class IncomingXmlSegment extends IncomingSegment{
    /**
     * 服务 ID
     */
    private Integer serviceId;
    /**
     * XML 消息内容
     */
    private String xmlPayload;
}
