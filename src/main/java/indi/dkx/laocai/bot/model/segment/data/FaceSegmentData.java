package indi.dkx.laocai.bot.model.segment.data;

/**
 * 表情消息段。
 * <p>
 * 表情是独立的协议段，不应和普通文本混在同一个字段里处理。
 * @param faceId 表情 ID
 * @param isLarge 是否使用大表情
 */
public record FaceSegmentData(
    String faceId,
    boolean isLarge
)implements SegmentData {
}


