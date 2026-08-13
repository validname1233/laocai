package indi.kyson.laocai.bot.model.event.data;

import indi.kyson.laocai.bot.model.segment.IncomingImageSegment;
import indi.kyson.laocai.bot.model.segment.MentionSegment;
import indi.kyson.laocai.bot.model.segment.Segment;
import indi.kyson.laocai.bot.model.segment.TextSegment;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 入站消息公共基类。
 * <p>
 * 群消息、好友消息虽然载体不同，但它们都共享一组消息字段和段落解析能力。
 */
@Data
public class IncomingMessage {
    private String messageScene;
    private Long peerId;
    private Long messageSeq;
    private Long senderId;
    private Long time;
    private List<Segment> segments;

    /**
     * 提取纯文本内容。
     * <p>
     * 上层 AI 和日志通常只关心可读文本，而不是完整的 segment 数组。
     */
    public String getPlainText() {
        return segments.stream()
        .filter(segment -> segment instanceof TextSegment)
        .map(segment -> ((TextSegment) segment).getText())
        .collect(Collectors.joining());
    }

    /**
     * 提取图片临时地址。
     * <p>
     * 图片需要单独下载和缓存，不能直接把 segment 当成最终可用资源。
     */
    public String[] getImageUrls() {
        return segments.stream()
        .filter(segment -> segment instanceof IncomingImageSegment)
        .map(segment -> ((IncomingImageSegment) segment).getTempUrl())
        .toArray(String[]::new);
    }

    /**
     * 提取被 @ 的用户 ID。
     * <p>
     * 部分监听器和过滤器需要基于 @ 关系做判断，而不是只看文本内容。
     */
    public long[] getMentionedUserIds() {
        return segments.stream()
        .filter(segment -> segment instanceof MentionSegment)
        .mapToLong(segment -> ((MentionSegment) segment).getUserId())
        .toArray();
    }
}


