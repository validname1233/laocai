package indi.kyson.laocai.bot.model.event;

import tools.jackson.databind.annotation.JsonDeserialize;
import indi.kyson.laocai.bot.deserializer.EventDeserializer;

/**
 * 统一事件载体。
 * <p>
 * 事件处理链要先拿到统一外壳，再根据 eventType 解析出具体 data 类型。
 * @param eventType 事件类型区分字段
 * @param time 事件 Unix 时间戳（秒）
 * @param selfId 机器人 QQ 号
 * @param data data 在不同 event_type 下有不同的具体类型
 * @param <T> data 的具体类型
 */
@JsonDeserialize(using = EventDeserializer.class)
public record Event<T>(
        String eventType,
        Long time,
        Long selfId,
        T data
) {

}


