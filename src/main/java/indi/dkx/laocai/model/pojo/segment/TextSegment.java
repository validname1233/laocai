package indi.dkx.laocai.model.pojo.segment;

import lombok.*;

/**
 * text 文本消息段
 */
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Data
public class TextSegment extends Segment<TextSegmentData> {
    public static TextSegment of(String text) {
        TextSegment segment = new TextSegment();
        segment.setType("text");
        segment.setData(new TextSegmentData(text));
        return segment;
    }
}
