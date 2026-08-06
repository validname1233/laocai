package indi.dkx.laocai.ai.tools;

import indi.dkx.laocai.ai.ChatClientFactory;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;

@SpringBootTest
class BotSenderToolTest {

    @Resource
    private ChatClientFactory chatClientFactory;

    @Resource
    private BotSenderTool botSenderTool;

    @Test
    void sendGroupAnnouncement() {
        String aiResponse = chatClientFactory.getChatPersonaClient(1098197034L)
                .prompt()
                .tools(botSenderTool)
                .toolContext(Map.of("groupId", 1098197034L))
                .user("牢财能帮我发一个群公告吗，内容你自己想")
                .call()
                .content();
    }
}