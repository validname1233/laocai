package indi.dkx.laocai.bot.model.segment;

import com.fasterxml.jackson.annotation.JsonIgnore;
import indi.dkx.laocai.bot.model.enums.ImageSubType;

/**
 * 入站的图片消息段。
 * <p>
 * 只在反序列化时产生，携带协议返回的临时资源信息。
 */
public final class IncomingImageSegment implements Segment {

    record Data(String resourceId, String tempUrl, Long width, Long height, String summary, ImageSubType subType) {
        IncomingImageSegment toSegment() {
            return new IncomingImageSegment(this);
        }
    }

    private final Data data;

    private IncomingImageSegment(Data data) {
        this.data = data;
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
    public String getResourceId() {
        return data.resourceId();
    }

    @JsonIgnore
    public String getTempUrl() {
        return data.tempUrl();
    }

    @JsonIgnore
    public Long getWidth() {
        return data.width();
    }

    @JsonIgnore
    public Long getHeight() {
        return data.height();
    }

    @JsonIgnore
    public String getSummary() {
        return data.summary();
    }

    @JsonIgnore
    public ImageSubType getSubType() {
        return data.subType();
    }
}
