package indi.dkx.laocai.handler;

import indi.dkx.laocai.annotation.Filter;
import indi.dkx.laocai.annotation.Listener;
import indi.dkx.laocai.core.BotSender;
import indi.dkx.laocai.model.pojo.incoming.message.IncomingGroupMessage;
import indi.dkx.laocai.model.pojo.incoming.segment.IncomingTextSegment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class MyEventHandler {

    private final BotSender botSender;

    @Listener
    @Filter("摸摸")
    public void handle(IncomingGroupMessage event) {
        event.getSegments().forEach(segment -> {
            if (segment instanceof IncomingTextSegment incomingTextSegment) {
                log.info("收到群消息: {}", incomingTextSegment.getData().getText());
                botSender.sendGroupMsg(event.getGroup().groupId(), List.of(
                        new IncomingTextSegment(new IncomingTextSegment.IncomingTextSegmentData("喵"))
                ));
            }
        });
    }
}
