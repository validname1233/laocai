package indi.dkx.laocai.model.pojo.incoming.segment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * text 文本消息段
 */
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Data
public class IncomingTextSegment extends IncomingSegment {
    private IncomingTextSegmentData data;

    public IncomingTextSegment(IncomingTextSegmentData data) {
        this.type = "text";
        this.data = data;
    }

    @Data
    @AllArgsConstructor
    public static class IncomingTextSegmentData {
        /**
         * 文本内容
         */
        private String text;
    }
}
