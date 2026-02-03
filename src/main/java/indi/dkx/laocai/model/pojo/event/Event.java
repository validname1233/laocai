package indi.dkx.laocai.model.pojo.event;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import indi.dkx.laocai.deserializer.EventDeserializer;

/**
 * 事件
 * @param eventType 类型区分字段
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
