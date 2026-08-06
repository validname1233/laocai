package indi.dkx.laocai.bot.model.segment;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Objects;

/**
 * 表情消息段。
 */
public final class FaceSegment implements Segment {

    record Data(String faceId, boolean isLarge) {
        FaceSegment toSegment() {
            return new FaceSegment(this);
        }
    }

    private final Data data;

    private FaceSegment(Data data) {
        this.data = data;
    }

    public static FaceSegment of(String faceId, boolean isLarge) {
        return new FaceSegment(new Data(Objects.requireNonNull(faceId), isLarge));
    }

    @Override
    public String getType() {
        return "face";
    }

    @Override
    public Data getData() {
        return data;
    }

    @JsonIgnore
    public String getFaceId() {
        return data.faceId();
    }

    @JsonIgnore
    public boolean isLarge() {
        return data.isLarge();
    }
}
