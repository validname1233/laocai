package indi.kyson.laocai.bot.model.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 性别枚举。
 * <p>
 * 上游协议返回的是字符串值，统一映射成枚举后，业务代码就不需要到处比较裸字符串。
 */
@Getter
@AllArgsConstructor
public enum Sex {
    MALE("male"),
    FEMALE("female"),
    UNKNOWN("unknown");

    @JsonValue
    private final String value;
}


