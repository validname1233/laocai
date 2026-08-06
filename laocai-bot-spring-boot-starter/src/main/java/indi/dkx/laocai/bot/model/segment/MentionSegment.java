package indi.dkx.laocai.bot.model.segment;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Objects;

/**
 * @提及消息段。
 */
public final class MentionSegment implements Segment {

    record Data(Long user_id) {
        MentionSegment toSegment() {
            return new MentionSegment(this);
        }
    }

    private final Data data;

    private MentionSegment(Data data) {
        this.data = data;
    }

    public static MentionSegment of(Long userId) {
        return new MentionSegment(new Data(Objects.requireNonNull(userId)));
    }

    @Override
    public String getType() {
        return "mention";
    }

    @Override
    public Data getData() {
        return data;
    }

    @JsonIgnore
    public Long getUserId() {
        return data.user_id();
    }
}
