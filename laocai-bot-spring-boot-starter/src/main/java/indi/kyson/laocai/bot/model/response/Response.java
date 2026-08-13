package indi.kyson.laocai.bot.model.response;

import indi.kyson.laocai.bot.model.enums.ResponseStatus;

/**
 * 统一响应包装。
 * <p>
 * 协议返回值不仅有业务数据，还带状态码和错误信息，统一封装后上层更容易判断结果。
 */
public record Response<T>(
    ResponseStatus status,
    Long retcode,
    T data,
    String message
) { }


