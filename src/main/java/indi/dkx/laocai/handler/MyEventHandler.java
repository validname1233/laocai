package indi.dkx.laocai.handler;

import indi.dkx.laocai.annotation.Filter;
import indi.dkx.laocai.annotation.Listener;
import indi.dkx.laocai.core.BotSender;
import indi.dkx.laocai.model.pojo.incoming.message.IncomingGroupMessage;
import indi.dkx.laocai.model.pojo.incoming.segment.IncomingMentionSegment;
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
    public void handle(IncomingGroupMessage message) {
        log.info("收到群消息: {}", message.getPlainText());
        if (message.getSenderId() == 1938437495) {
            botSender.sendGroupMsg(message.getGroup().groupId(), List.of(
                    IncomingMentionSegment.of(message.getSenderId()),
                    IncomingTextSegment.of(" 哈！")
            ));
        } else {
            botSender.sendGroupMsg(message.getGroup().groupId(), List.of(
                    IncomingMentionSegment.of(message.getSenderId()),
                    IncomingTextSegment.of(" 喵")
            ));
        }
    }
}
