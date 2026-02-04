package indi.dkx.laocai.handler.test;

import indi.dkx.laocai.bot.annotation.Filter;
import indi.dkx.laocai.bot.annotation.Listener;
import indi.dkx.laocai.bot.core.BotSender;
import indi.dkx.laocai.bot.model.event.Event;
import indi.dkx.laocai.bot.model.event.data.IncomingFriendMessage;
import indi.dkx.laocai.bot.model.event.data.IncomingGroupMessage;
import indi.dkx.laocai.bot.model.segment.MentionSegment;
import indi.dkx.laocai.bot.model.segment.TextSegment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class TestHandler {

    private final BotSender botSender;

    @Listener
    @Filter("摸摸")
    public void handleGroup(Event<IncomingGroupMessage> event) {
        IncomingGroupMessage message = event.data();
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
        IncomingFriendMessage message = event.data();
        log.info("收到好友消息: {}", message.getPlainText());
        if (message.getPlainText().equals("D")) {
            Thread.sleep(5000L);
        }
        botSender.sendPrivateMsg(message.getSenderId(), List.of(
                new TextSegment(message.getPlainText())
        ));
    }
}
