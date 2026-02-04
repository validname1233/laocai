package indi.dkx.laocai.model.pojo.segment;

import lombok.Data;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import indi.dkx.laocai.deserializer.SegmentDeserializer;

@JsonDeserialize(using = SegmentDeserializer.class)
@Data
public abstract class Segment<T> {
    private final String type;
    private final T data;
    protected Segment(String type, T data) {
        this.type = type;
        this.data = data;
    }
}
