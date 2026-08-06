package indi.dkx.laocai.bot.model.segment;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;

/**
 * 入站的引用回复消息段。
 * <p>
 * 只在反序列化时产生，携带被回复消息的完整内容。
 */
public final class IncomingReplySegment implements Segment {

    record Data(Long messageSeq, Long senderId, String senderName, Long time, List<Segment> segments) {
        IncomingReplySegment toSegment() {
            return new IncomingReplySegment(this);
        }
    }

    private final Data data;

    private IncomingReplySegment(Data data) {
        this.data = data;
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

    @JsonIgnore
    public Long getSenderId() {
        return data.senderId();
    }

    @JsonIgnore
    public String getSenderName() {
        return data.senderName();
    }

    @JsonIgnore
    public Long getTime() {
        return data.time();
    }

    @JsonIgnore
    public List<Segment> getSegments() {
        return data.segments();
    }
}
