package indi.dkx.laocai.model.pojo.incoming.segment;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * video 视频消息段
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class IncomingVideoSegment extends IncomingSegment {
    /**
     * 资源 ID
     */
    private String resourceId;
    /**
     * 临时 URL
     */
    private String tempUrl;
    /**
     * 视频宽度
     */
    private Integer width;
    /**
     * 视频高度
     */
    private Integer height;
    /**
     * 视频时长(秒)
     */
    private Integer duration;
}
