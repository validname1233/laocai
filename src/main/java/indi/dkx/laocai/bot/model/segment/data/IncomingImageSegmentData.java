package indi.dkx.laocai.bot.model.segment.data;

import indi.dkx.laocai.bot.model.enums.ImageSubType;

/**
 * 入站图片消息段。
 * <p>
 * 图片消息需要把资源 ID、临时 URL 和展示信息一起保留下来，后续才能下载、缓存和重放。
 * @param resourceId 图片资源 ID
 * @param tempUrl 图片临时地址
 * @param width 图片宽度
 * @param height 图片高度
 * @param summary 图片摘要
 * @param subType 图片子类型
 */
public record IncomingImageSegmentData(
        String resourceId,
        String tempUrl,
        Long width,
        Long height,
        String summary,
        ImageSubType subType
) implements SegmentData {
}


