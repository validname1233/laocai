package indi.dkx.laocai.ai;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest
public class ChatClientTest {

    @Resource
    private ChatClient.Builder chatClientBuilder;


    @Test
    void test() {
        String content = chatClientBuilder.defaultAdvisors(new SimpleLoggerAdvisor())
                .build()
                .prompt()
                .user("1000-7=?")
                .call()
                .content();

        log.info(content);
    }

}
