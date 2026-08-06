package indi.dkx.laocai.bot.model.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 图片子类型。
 * <p>
 * 图片消息需要保留协议侧的细分类别，方便后续序列化回写和日志排查。
 */
@Getter
@AllArgsConstructor
public enum ImageSubType {
    NORMAL("normal"),
    STICKER("sticker");

    @JsonValue
    private final String value;
}


