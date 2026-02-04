package indi.dkx.laocai.bot.model.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Role {
    OWNER("owner"),
    ADMIN("admin"),
    MEMBER("member");

    // @JsonValue 告诉 Jackson：
    // 1. 序列化时（转JSON）：调用这个字段的值作为 JSON 字符串
    // 2. 反序列化时（转对象）：拿 JSON 字符串跟这个字段比对，这就不用一个个写 @JsonProperty 了
    @JsonValue
    private final String value;
}
