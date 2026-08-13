package indi.kyson.laocai.bot.model.segment;

import com.fasterxml.jackson.annotation.JsonIgnore;
import tools.jackson.databind.JsonNode;

/**
 * 未知类型的消息段。
 * <p>
 * 协议新增或变更 segment 类型时不应让整条消息反序列化失败，先原样保留原始节点。
 */
public final class UnknownSegment implements Segment {

    record Data(String type, JsonNode raw) {
        UnknownSegment toSegment() {
            return new UnknownSegment(this);
        }
    }

    private final Data data;

    private UnknownSegment(Data data) {
        this.data = data;
    }

    static UnknownSegment of(String type, JsonNode raw) {
        return new UnknownSegment(new Data(type, raw));
    }

    @Override
    public String getType() {
        return data.type();
    }

    @Override
    public Data getData() {
        return data;
    }

    @JsonIgnore
    public JsonNode getRaw() {
        return data.raw();
    }
}
