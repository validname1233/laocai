package indi.dkx.laocai.handler;

import indi.dkx.laocai.annotation.Filter;
import indi.dkx.laocai.annotation.Listener;
import indi.dkx.laocai.core.BotSender;
import indi.dkx.laocai.model.pojo.event.Event;
import indi.dkx.laocai.model.pojo.message.IncomingFriendMessage;
import indi.dkx.laocai.model.pojo.message.IncomingGroupMessage;
import indi.dkx.laocai.model.pojo.segment.MentionSegment;
import indi.dkx.laocai.model.pojo.segment.TextSegment;
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
    public void handleGroup(Event<IncomingGroupMessage> event) {
        IncomingGroupMessage message = event.getData();
        log.info("收到群消息: {}", message.getPlainText());
        if (message.getSenderId() == 1938437495) {
            botSender.sendGroupMsg(message.getGroup().groupId(), List.of(
                    new MentionSegment(message.getSenderId()),
                    new TextSegment(" 哈！")
            ));
        } else {
            botSender.sendGroupMsg(message.getGroup().groupId(), List.of(
                    new MentionSegment(message.getSenderId()),
                    new TextSegment(" 喵")
            ));
        }
    }

    @Listener
    public void handleFriend(Event<IncomingFriendMessage> event) throws InterruptedException {
        IncomingFriendMessage message = event.getData();
        log.info("收到好友消息: {}", message.getPlainText());
        if (message.getPlainText().equals("D")) {
            Thread.sleep(5000L);
        }
        botSender.sendPrivateMsg(message.getSenderId(), List.of(
                new TextSegment(message.getPlainText())
        ));
    }
}
