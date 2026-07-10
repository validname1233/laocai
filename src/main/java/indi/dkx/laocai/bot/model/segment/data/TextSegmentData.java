package indi.dkx.laocai.bot.model.segment.data;

/**
 * 文本消息段。
 * <p>
 * 文本是最常见的消息载体，单独成型能让纯文本提取逻辑更直接。
 * @param text 文本内容
 */
public record TextSegmentData(
    String text
)implements SegmentData {
}


