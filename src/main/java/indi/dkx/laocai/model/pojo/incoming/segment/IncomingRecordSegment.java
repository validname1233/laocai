package indi.dkx.laocai.model.pojo.incoming.segment;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * record 语音消息段
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class IncomingRecordSegment extends IncomingSegment {
    /**
     * 语音文件 ID
     */
    private String resourceId;
    /**
     * 语音文件临时 URL
     */
    private String tempUrl;
    /**
     * 语音时长（秒）
     */
    private Integer duration;
}
