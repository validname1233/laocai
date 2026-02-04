package indi.dkx.laocai.bot.model.segment;

import lombok.*;
import indi.dkx.laocai.bot.model.segment.data.TextSegmentData;

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
