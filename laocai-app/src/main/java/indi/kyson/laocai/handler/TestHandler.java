package indi.kyson.laocai.handler;

import indi.kyson.laocai.bot.core.Bot;
import indi.kyson.laocai.bot.core.annotation.Filter;
import indi.kyson.laocai.bot.core.annotation.Listener;
import indi.kyson.laocai.bot.core.event.FriendMessageEvent;
import indi.kyson.laocai.bot.core.event.GroupMessageEvent;
import indi.kyson.laocai.bot.core.segment.MentionSegment;
import indi.kyson.laocai.bot.core.segment.OutgoingRecordSegment;
import indi.kyson.laocai.bot.core.segment.TextSegment;
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

    private final Bot bot;

    @Listener
    @Filter(value = "^[1-9]\\d{4,11}@qq\\.com(\\.cn)?$", targets = {@Filter.Targets(groups = {1234567890L, 1234567890L})})
    public void handleGroup(GroupMessageEvent event) {
        // 这个 handler 只是一个最小回声示例，用来验证过滤器和发送链路是否可用。
        log.info("收到群消息: {}", event.getPlainText());
        if (event.getSenderId() == 1938437495) {
            bot.sendGroupMsg(
                event.getGroup().getGroupId(),
                List.of(
                    MentionSegment.of(event.getSenderId()),
                    TextSegment.of(" 哈！")
            )).block();
        } else {
            bot.sendGroupMsg(event.getGroup().getGroupId(), List.of(
                    MentionSegment.of(event.getSenderId()),
                    TextSegment.of(" 喵")
            )).block();
        }
    }

    @Listener
    public void handleFriend(FriendMessageEvent event) throws InterruptedException {
        log.info("收到好友消息: {}", event.getPlainText());
        if (event.getPlainText().equals("D")) {
            // 故意保留一个慢回复分支，用来观察调用链是否会被阻塞。
            Thread.sleep(5000L);
        }
        bot.sendPrivateMsg(event.getSenderId(), List.of(
            // TextSegment.of(event.getPlainText())
            OutgoingRecordSegment.of("file://D:\\QQbot\\laocai-llbot-java\\tmp\\audios\\output_ja.wav")
        )).block();
    }
}

