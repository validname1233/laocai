package indi.dkx.laocai.bot.model.event.data;


import indi.dkx.laocai.bot.model.segment.Segment;
import indi.dkx.laocai.bot.model.segment.data.TextSegmentData;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;


@Data
public class IncomingMessage {
    private String messageScene;
    private Long peerId;
    private Long messageSeq;
    private Long senderId;
    private Long time;
    private List<Segment<?>> segments;

    public String getPlainText() {
        return segments.stream()
        .map(Segment::getData)
        .filter(data -> data instanceof TextSegmentData)
        .map(data -> ((TextSegmentData) data).text())
        .collect(Collectors.joining());
    }
}
