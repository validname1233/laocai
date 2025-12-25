package indi.dkx.laocai.model.pojo.incoming.segment;

import indi.dkx.laocai.model.enums.ImageSubType;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class IncomingImageSegment extends IncomingSegment {
    /**
     * 资源ID
     */
    private String resourceId;
    /**
     * 临时 URL
     */
    private String tempUrl;
    /**
     * 图片宽度
     */
    private Integer width;
    /**
     * 图片高度
     */
    private Integer height;
    /**
     * 图片预览文本
     */
    private String summary;
    /**
     * 图片子类型
     */
    private ImageSubType subType;
}
