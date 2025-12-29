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
        @JsonSubTypes.Type(value = MessageReceiveEvent.class, name = "message_receive"), // 收到消息
})
@Data
public abstract class Event {
    private Long time;
    private Long selfId;
    private String eventType;

    // 注意：这里不定义 'data'，因为不同子类的 data 结构完全不一样
    // 让子类自己去定义 data 的类型
}
