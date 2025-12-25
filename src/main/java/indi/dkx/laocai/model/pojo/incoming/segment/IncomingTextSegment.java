package indi.dkx.laocai.model.pojo.incoming.segment;

import lombok.*;

/**
 * text 文本消息段
 */
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Data
public class IncomingTextSegment extends IncomingSegment {

    private IncomingTextSegmentData data;

    /**
     * @param text 文本内容
     */
    public record IncomingTextSegmentData(String text) { }

    public static IncomingTextSegment of(String text) {
        IncomingTextSegment segment = new IncomingTextSegment();
        segment.setType("text");
        segment.setData(new IncomingTextSegmentData(text));
        return segment;
    }
}
