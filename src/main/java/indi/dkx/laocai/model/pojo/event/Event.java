package indi.dkx.laocai.model.pojo.event;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;

// 1. 开启多态支持
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY, // 使用 JSON 中已有的字段判断
        property = "event_type",                     // 字段名是 event_type
        visible = true                               // 设为 true，子类里也能读到这个字段
)
// 2. 定义映射关系：值 -> 类
@JsonSubTypes({
        @JsonSubTypes.Type(value = GroupMessageReceiveEvent.class, name = "message_receive"), // 消息接收事件
        // TODO: 群文件上传事件
})
@Data
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
