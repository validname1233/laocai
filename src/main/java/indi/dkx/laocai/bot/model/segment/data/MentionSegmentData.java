package indi.dkx.laocai.bot.model.segment.data;

/**
 * @@ 消息段。
 * <p>
 * 被 @ 的对象是过滤和提示逻辑里的关键输入，需要单独保留用户 ID。
 * @param user_id 被 @ 的用户 ID
 */
public record MentionSegmentData(
    Long user_id
)implements SegmentData {
}


