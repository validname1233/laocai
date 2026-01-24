package indi.dkx.laocai.model.pojo.segment;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;

// 1. 开启多态支持
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY, // 使用 JSON 中已有的字段判断
        property = "type",                     // 字段名是 event_type
        visible = true                               // 设为 true，子类里也能读到这个字段
)
// 2. 定义映射关系：值 -> 类
@JsonSubTypes({
        @JsonSubTypes.Type(value = TextSegment.class, name = "text"), // 收到文本消息
        @JsonSubTypes.Type(value = MentionSegment.class, name = "mention"), // 收到@消息
        @JsonSubTypes.Type(value = FaceSegment.class, name = "face"), // 收到表情消息
        @JsonSubTypes.Type(value = ReplySegment.class, name = "reply"), // 收到回复消息
        // TODO: 收到@全体成员消息
        // TODO: 收到图片消息
        // TODO: 收到语音消息
        // TODO: 收到视频消息
        // TODO: 收到文件消息
        // TODO: 收到合并转发消息
        // TODO: 收到市场表情消息
        // TODO: 收到小程序消息
        // TODO: 收到XML消息
})
@Data
public class Segment<T> {
    private String type;
    private T data;
}
