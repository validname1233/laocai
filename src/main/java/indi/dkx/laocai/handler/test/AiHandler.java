package indi.dkx.laocai.handler.test;

import indi.dkx.laocai.bot.annotation.Filter;
import indi.dkx.laocai.bot.annotation.Listener;
import indi.dkx.laocai.bot.core.BotSender;
import indi.dkx.laocai.bot.model.event.Event;
import indi.dkx.laocai.bot.model.event.data.IncomingGroupMessage;
import indi.dkx.laocai.bot.model.segment.Segments;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class AiHandler {

    private final ChatClient chatClient;

    private final BotSender botSender;

    public AiHandler(
            ChatClient.Builder chatClientBuilder,
            BotSender botSender
    ) {
        this.chatClient = chatClientBuilder.build();
        this.botSender = botSender;
    }

    @Listener
    @Filter(value = "1000-7=\\?")
    public void test(Event<IncomingGroupMessage> event) {
        IncomingGroupMessage message = event.data();
        log.info("收到群消息: {}", message);

        String aiResponse = chatClient.prompt()
                .user(message.getPlainText())
                .call()
                .content();

        botSender.sendGroupMsg(message.getGroup().groupId(), List.of(
                Segments.mention(message.getSenderId()),
                Segments.text(" " + aiResponse)));
    }
}
