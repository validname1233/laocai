package indi.dkx.laocai.model.pojo.segment;

/**
 * face 表情消息段 data
 * @param faceId 表情ID
 * @param isLarge 是否为超级表情
 */
public record FaceSegmentData(
        String faceId,
        Boolean isLarge
) {
}
