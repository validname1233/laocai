package indi.dkx.laocai.bot.model.segment;

import com.fasterxml.jackson.annotation.JsonIgnore;
import indi.dkx.laocai.bot.model.enums.ImageSubType;

import java.util.Objects;

/**
 * 出站的图片消息段。
 * <p>
 * 发送时通过 uri 指明图片来源，和入站的临时资源结构不同，协议 type 都是 image。
 */
public final class OutgoingImageSegment implements Segment {

    record Data(String uri, ImageSubType subType, String summary) {
        OutgoingImageSegment toSegment() {
            return new OutgoingImageSegment(this);
        }
    }

    private final Data data;

    private OutgoingImageSegment(Data data) {
        this.data = data;
    }

    public static OutgoingImageSegment of(String uri, ImageSubType subType, String summary) {
        return new OutgoingImageSegment(new Data(Objects.requireNonNull(uri), subType, summary));
    }

    @Override
    public String getType() {
        return "image";
    }

    @Override
    public Data getData() {
        return data;
    }

    @JsonIgnore
    public String getUri() {
        return data.uri();
    }

    @JsonIgnore
    public ImageSubType getSubType() {
        return data.subType();
    }

    @JsonIgnore
    public String getSummary() {
        return data.summary();
    }
}
