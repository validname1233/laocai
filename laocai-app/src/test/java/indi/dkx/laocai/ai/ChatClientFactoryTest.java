package indi.dkx.laocai.ai;


import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.content.Media;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.MimeTypeUtils;

@Slf4j
@SpringBootTest
public class ChatClientFactoryTest {

    @Resource
    private ChatClientFactory chatClientFactory;

    @Test
    void testImage() {
        ChatClient chatClient = chatClientFactory.getChatClient(1);
        String content = chatClient
                .prompt()
                .user(promptUserSpec -> promptUserSpec
                        .text("这张图里是什么？")
                        .media(Media.builder()
                                .mimeType(MimeTypeUtils.IMAGE_PNG)
//                                .data(URI.create("https://multimedia.nt.qq.com.cn/download?appid=1407&fileid=EhRXwu0vphvhDJDwRLw7g9hxOLXxjhjEowQg_wootfrb6qGulAMyBHByb2RQgL2jAVoQ8Q9BoyIggeOZVNMclTrJeXoCUJCCAQJneg&spec=0&rkey=CAQSMKUxOcDDAlmWXvm-Ds03bNFoa_ssfyKaWrnd-61AzWpcwt5vMS2janD_wvfbdRzUSw"))
//                                .data(URI.create("https://www.baidu.com/img/PCtm_d9c8750bed0b3c7d089fa7d55720d6cf.png"))
                                .data(new ClassPathResource("kakuya.png"))
                                .build()))
                .call()
                .content();
        log.info(content);
    }
}
