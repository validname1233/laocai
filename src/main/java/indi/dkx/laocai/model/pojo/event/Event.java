package indi.dkx.laocai.model.pojo.event;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import indi.dkx.laocai.deserializer.EventDeserializer;
import lombok.Builder;
import lombok.Data;

@JsonDeserialize(using = EventDeserializer.class)
@Data
@Builder
public class Event<T> {
    /**
     * 类型区分字段
     */
    private String eventType;
    /**
     * 事件 Unix 时间戳（秒）
     */
    private Long time;
    /**
     * 机器人 QQ 号
     */
    private Long selfId;
    /**
     * data 在不同 event_type 下有不同的具体类型
     */
    private T data;
}
