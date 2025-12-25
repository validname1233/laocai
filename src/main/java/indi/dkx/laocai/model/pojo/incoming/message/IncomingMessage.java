package indi.dkx.laocai.model.pojo.incoming.message;


import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import indi.dkx.laocai.model.pojo.incoming.segment.IncomingSegment;
import lombok.Data;

import java.util.List;


@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "message_scene",
        visible = true
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = IncomingFriendMessage.class, name = "friend"),
        @JsonSubTypes.Type(value = IncomingGroupMessage.class, name = "group")
})
@Data
public abstract class IncomingMessage {
    private String messageScene;
    private Long peerId;
    private Long messageSeq;
    private Long senderId;
    private Long time;
    private List<IncomingSegment> segments;
}
