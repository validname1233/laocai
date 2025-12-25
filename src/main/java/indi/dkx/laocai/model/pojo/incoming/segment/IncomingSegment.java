package indi.dkx.laocai.model.pojo.incoming.segment;

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
        @JsonSubTypes.Type(value = IncomingTextSegment.class, name = "text"), // 收到消息
        @JsonSubTypes.Type(value = IncomingMentionSegment.class, name = "mention"), // 收到消息
        @JsonSubTypes.Type(value = IncomingMentionAllSegment.class, name = "mention_all"), // 收到消息
        @JsonSubTypes.Type(value = IncomingFaceSegment.class, name = "face"), // 收到消息
        @JsonSubTypes.Type(value = IncomingReplySegment.class, name = "reply"), // 收到消息
        @JsonSubTypes.Type(value = IncomingImageSegment.class, name = "image"), // 收到消息
        @JsonSubTypes.Type(value = IncomingRecordSegment.class, name = "record"), // 收到消息
        @JsonSubTypes.Type(value = IncomingVideoSegment.class, name = "video"), // 收到消息
        @JsonSubTypes.Type(value = IncomingFileSegment.class, name = "file"), // 收到消息
        @JsonSubTypes.Type(value = IncomingForwardSegment.class, name = "forward"), // 收到消息
        @JsonSubTypes.Type(value = IncomingMarketFaceSegment.class, name = "market_face"), // 收到消息
        @JsonSubTypes.Type(value = IncomingLightAppSegment.class, name = "light_app"), // 收到消息
        @JsonSubTypes.Type(value = IncomingXmlSegment.class, name = "xml"), // 收到消息
})
@Data
public abstract class IncomingSegment {
    String type;
}
