package indi.kyson.laocai.bot.model.segment;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;

class SegmentTest {

    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    @Test
    void textSegmentSerializesToTypeAndDataShape() {
        Segment segment = TextSegment.of("hi");

        var tree = jsonMapper.valueToTree(segment);

        assertThat(tree.get("type").asString()).isEqualTo("text");
        assertThat(tree.get("data").get("text").asString()).isEqualTo("hi");
        assertThat(tree.size()).isEqualTo(2);
    }

    @Test
    void textSegmentRoundTripsThroughDeserialization() {
        Segment segment = jsonMapper.readValue("{\"type\":\"text\",\"data\":{\"text\":\"hi\"}}", Segment.class);

        assertThat(segment).isInstanceOf(TextSegment.class);
        assertThat(((TextSegment) segment).getText()).isEqualTo("hi");
    }

    @Test
    void unknownTypeFallsBackToUnknownSegmentInsteadOfThrowing() {
        Segment segment = jsonMapper.readValue("{\"type\":\"foobar\",\"data\":{\"x\":1}}", Segment.class);

        assertThat(segment).isInstanceOf(UnknownSegment.class);
        assertThat(segment.getType()).isEqualTo("foobar");
    }
}
