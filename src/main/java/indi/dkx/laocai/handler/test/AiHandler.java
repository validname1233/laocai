package indi.dkx.laocai.handler.test;

import indi.dkx.laocai.ai.ChatClientFactory;
import indi.dkx.laocai.ai.tools.BotSenderTool;
import indi.dkx.laocai.bot.annotation.Filter;
import indi.dkx.laocai.bot.annotation.Listener;
import indi.dkx.laocai.bot.core.BotSender;
import indi.dkx.laocai.bot.model.event.Event;
import indi.dkx.laocai.bot.model.event.data.IncomingGroupMessage;
import indi.dkx.laocai.bot.model.segment.Segments;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiHandler {

    private final ChatClientFactory chatClientFactory;

    private final BotSender botSender;

    private final BotSenderTool botSenderTool;


    @Listener
    @Filter(value = "^[\\.。]ai.*")
    public void test(Event<IncomingGroupMessage> event) {
        // AI 首先判断是否需要回复
        // 如果不需要回复，则直接返回
        // 如果需要回复，则调用 AI 模型生成回复
        IncomingGroupMessage message = event.data();
        log.info("收到群消息: {}", message);

        String content = message.getPlainText().substring(3).strip();

        String aiResponse = chatClientFactory.getChatClient(message.getGroup().groupId())
                .prompt()
                .tools(botSenderTool)
                .toolContext(Map.of("groupId", message.getGroup().groupId()))
                .user(content)
                .call()
                .content();

        botSender.sendGroupMsg(message.getGroup().groupId(), List.of(
                Segments.mention(message.getSenderId()),
                Segments.text(" " + aiResponse)));
    }
}
