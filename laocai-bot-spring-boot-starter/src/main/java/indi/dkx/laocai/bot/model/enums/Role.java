package indi.dkx.laocai.bot.model.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 群权限等级。
 * <p>
 * 群成员权限判断依赖协议原始枚举值，统一收敛到一个枚举可以避免分支里直接散写字符串。
 */
@Getter
@AllArgsConstructor
public enum Role {
    OWNER("owner"),
    ADMIN("admin"),
    MEMBER("member");

    @JsonValue
    private final String value;
}


