package indi.dkx.laocai.ai.model;

import java.util.List;

/**
 * 群聊上下文记录。
 * <p>
 * AI 决策和回复生成都依赖最近对话历史，单独封装后更容易序列化到 Redis。
 * @param time 消息时间戳
 * @param senderId 发送者 ID
 * @param content 消息文本
 * @param imageIds 关联图片资源 ID 列表
 */
public record ChatRecord(
        long time,
        long senderId,
        String content,
        List<String> imageIds
) {
}