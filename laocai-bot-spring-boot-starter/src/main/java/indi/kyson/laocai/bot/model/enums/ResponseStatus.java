package indi.kyson.laocai.bot.model.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 响应状态。
 * <p>
 * 请求是否成功不能只看 HTTP 层，还要看协议返回的业务状态。
 */
@Getter
@AllArgsConstructor
public enum ResponseStatus {
    OK("ok"),
    FAILED("failed");

    @JsonValue
    private final String value;
}


