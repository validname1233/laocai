package indi.dkx.laocai.bot.model.event.data;


import indi.dkx.laocai.bot.model.segment.Segment;
import indi.dkx.laocai.bot.model.segment.TextSegment;
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
                .filter(seg -> seg instanceof TextSegment)
                .map(seg -> ((TextSegment) seg).getData().text())
                .collect(Collectors.joining());
    }
}
