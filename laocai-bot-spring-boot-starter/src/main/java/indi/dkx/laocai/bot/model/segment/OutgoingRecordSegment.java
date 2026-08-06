package indi.dkx.laocai.bot.model.segment;

import java.util.Objects;

public final class OutgoingRecordSegment implements Segment {

    record Data(String uri) {
    }

    private final Data data;

    private OutgoingRecordSegment(Data data) {
        this.data = data;
    }

    public static OutgoingRecordSegment of(String uri) {
        return new OutgoingRecordSegment(new Data(Objects.requireNonNull(uri)));
    }

    @Override
    public Object getData() {
        return data;
    }

    @Override
    public String getType() {
        return "record";
    }
    
}
