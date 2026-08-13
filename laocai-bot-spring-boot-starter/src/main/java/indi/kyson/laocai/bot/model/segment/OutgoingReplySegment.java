package indi.kyson.laocai.bot.model.segment;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Objects;

/**
 * 出站的引用回复消息段。
 * <p>
 * 发送时只需要指明被回复消息的 messageSeq，协议字段名为 shouldReply。
 */
public final class OutgoingReplySegment implements Segment {

    record Data(Long messageSeq) {
        OutgoingReplySegment toSegment() {
            return new OutgoingReplySegment(this);
        }
    }

    private final Data data;

    private OutgoingReplySegment(Data data) {
        this.data = data;
    }

    public static OutgoingReplySegment of(Long messageSeq) {
        return new OutgoingReplySegment(new Data(Objects.requireNonNull(messageSeq)));
    }

    @Override
    public String getType() {
        return "reply";
    }

    @Override
    public Data getData() {
        return data;
    }

    @JsonIgnore
    public Long getMessageSeq() {
        return data.messageSeq();
    }
}
