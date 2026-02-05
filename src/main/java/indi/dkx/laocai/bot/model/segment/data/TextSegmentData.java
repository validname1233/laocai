package indi.dkx.laocai.bot.model.segment.data;

/**
 * text 文本消息段 data
 * @param text 文本内容
 */
public record TextSegmentData(
    String text
)implements SegmentData {
}
