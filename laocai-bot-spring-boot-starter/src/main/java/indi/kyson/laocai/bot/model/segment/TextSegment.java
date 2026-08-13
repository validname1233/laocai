package indi.kyson.laocai.bot.model.segment;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Objects;

/**
 * 纯文本消息段。
 */
public final class TextSegment implements Segment {

    record Data(String text) {
        TextSegment toSegment() {
            return new TextSegment(this);
        }
    }

    private final Data data;

    private TextSegment(Data data) {
        this.data = data;
    }

    public static TextSegment of(String text) {
        return new TextSegment(new Data(Objects.requireNonNull(text)));
    }

    @Override
    public String getType() {
        return "text";
    }

    @Override
    public Data getData() {
        return data;
    }

    @JsonIgnore
    public String getText() {
        return data.text();
    }
}
