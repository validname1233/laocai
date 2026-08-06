package indi.dkx.laocai.handler;

import indi.dkx.laocai.bot.annotation.Filter;
import indi.dkx.laocai.bot.annotation.Listener;
import indi.dkx.laocai.bot.core.BotSender;
import indi.dkx.laocai.bot.model.event.Event;
import indi.dkx.laocai.bot.model.event.data.IncomingFriendMessage;
import indi.dkx.laocai.bot.model.event.data.IncomingGroupMessage;
import indi.dkx.laocai.bot.model.segment.MentionSegment;
import indi.dkx.laocai.bot.model.segment.OutgoingRecordSegment;
import indi.dkx.laocai.bot.model.segment.TextSegment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 测试用消息处理器。
 * <p>
 * 保留一个简单回声链路，可以快速验证监听、过滤和发送这几段基础能力。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TestHandler {

    private final BotSender botSender;

    @Listener
    @Filter(value = "^[1-9]\\d{4,11}@qq\\.com(\\.cn)?$", targets = {@Filter.Targets(groups = {1234567890L, 1234567890L})})
    public void handleGroup(Event<IncomingGroupMessage> event) {
        // 这个 handler 只是一个最小回声示例，用来验证过滤器和发送链路是否可用。
        IncomingGroupMessage message = event.data();
        log.info("收到群消息: {}", message.getPlainText());
        if (message.getSenderId() == 1938437495) {
            botSender.sendGroupMsg(message.getGroup().groupId(), List.of(
                    MentionSegment.of(message.getSenderId()),
                    TextSegment.of(" 哈！")
            )).block();
        } else {
            botSender.sendGroupMsg(message.getGroup().groupId(), List.of(
                    MentionSegment.of(message.getSenderId()),
                    TextSegment.of(" 喵")
            )).block();
        }
    }

    @Listener
    public void handleFriend(Event<IncomingFriendMessage> event) throws InterruptedException {
        IncomingFriendMessage message = event.data();
        log.info("收到好友消息: {}", message.getPlainText());
        if (message.getPlainText().equals("D")) {
            // 故意保留一个慢回复分支，用来观察调用链是否会被阻塞。
            Thread.sleep(5000L);
        }
        botSender.sendPrivateMsg(message.getSenderId(), List.of(
            // TextSegment.of(message.getPlainText())
            OutgoingRecordSegment.of("file://D:\\QQbot\\laocai-llbot-java\\tmp\\audios\\output_ja.wav")
        )).block();
    }
}


