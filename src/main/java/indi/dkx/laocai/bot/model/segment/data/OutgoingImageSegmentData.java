package indi.dkx.laocai.bot.model.segment.data;

import indi.dkx.laocai.bot.model.enums.ImageSubType;

/**
 * 出站图片消息段。
 * <p>
 * 发送侧必须显式携带图片 URI 和子类型，才能让协议端正确组装消息。
 * @param uri 图片 URI
 * @param subType 图片子类型
 * @param summary 图片摘要
 */
public record OutgoingImageSegmentData(String uri, ImageSubType subType, String summary) implements SegmentData {
}


