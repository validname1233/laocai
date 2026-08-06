package indi.dkx.laocai.ai;

import com.openai.client.OpenAIClient;
// import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.content.Media;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.MimeTypeUtils;

@Slf4j
@SpringBootTest
public class ChatModelTest {

    @Resource
    private OpenAiChatModel openAiChatModel;

    @Resource
    private DeepSeekChatModel deepSeekChatModel;

//     @Test
//     void testPureAPI() {
//         OpenAIClient client = OpenAIOkHttpClient.builder()
//                 .baseUrl("")
//                 .apiKey("")
//                 .build();

//         ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
//                 .addUserMessage("黑洞是什么")
//                 .model("gpt-5.5")
//                 .build();
// //        try (StreamResponse<ChatCompletionChunk> streamResponse = client.chat().completions().createStreaming(params)) {
// //            streamResponse.stream().forEach(chatCompletionChunk -> log.info(chatCompletionChunk.choices().getFirst().delta().content().orElse("No content")));
// //            log.info("Stream completed");
// //        }
//         ChatCompletion chatCompletion = client.chat().completions().create(params);
//         log.info(chatCompletion.choices().getFirst().message().content().orElse("No content"));
//     }

    @Test
    void testOpenAiChatModel() {
        long start = System.currentTimeMillis();
        String content = openAiChatModel.call(
                UserMessage.builder()
                .text("这张图里有什么")
                .media(Media.builder()
                        .mimeType(MimeTypeUtils.IMAGE_PNG)
                        .data(new ClassPathResource("kakuya.png"))
                        .build())
                .build()
        );
        long end = System.currentTimeMillis();
        log.info("Time taken: {} seconds", (end - start) / 1000.0);
        log.info(content);
    }

    @Test
    void testDeepSeekChatModel() {
        String content = deepSeekChatModel.call("黑洞是什么");
        log.info(content);
    }
}
