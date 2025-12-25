package indi.dkx.laocai.model.pojo.incoming.segment;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * light_app 小程序消息段
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class IncomingLightAppSegment extends IncomingSegment {
    /**
     * 小程序名称
     */
    private String appName;
    /**
     * 小程序 JSON 数据
     */
    private String jsonPayload;
}
