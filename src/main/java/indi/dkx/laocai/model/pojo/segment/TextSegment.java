package indi.dkx.laocai.model.pojo.segment;

import lombok.*;
import indi.dkx.laocai.model.pojo.data.TextSegmentData;

/**
 * text 文本消息段
 */
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Data
public class TextSegment extends Segment<TextSegmentData> {
    public TextSegment(String text){
        super("text", new TextSegmentData(text));
    }
}
