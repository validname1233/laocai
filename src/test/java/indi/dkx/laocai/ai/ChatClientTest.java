package indi.dkx.laocai.ai;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest
public class ChatClientTest {

    @Resource
    private ChatClientFactory chatClientFactory;


    @Test
    void test() {
        ChatClient chatClient = chatClientFactory.getChatClient(1);
        String content = chatClient.prompt()
                .user("我叫乙骨忧太")
                .call()
                .content();

        String content1 = chatClient.prompt()
                .user("我叫什么？")
                .call()
                .content();


        log.info(content);
    }

}
