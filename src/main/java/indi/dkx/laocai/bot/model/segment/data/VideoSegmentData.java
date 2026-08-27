package indi.dkx.laocai.bot.model.segment.data;

public record VideoSegmentData(
        String uri,
        String thumbUri
) implements SegmentData {
}
