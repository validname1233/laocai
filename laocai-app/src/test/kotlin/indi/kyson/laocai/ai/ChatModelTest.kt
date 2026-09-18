package indi.kyson.laocai.ai

import jakarta.annotation.Resource
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.ai.chat.messages.UserMessage
import org.springframework.ai.content.Media
import org.springframework.ai.chat.model.ChatModel
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.springframework.ai.openai.OpenAiChatModel
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.io.ClassPathResource
import org.springframework.util.MimeTypeUtils

@SpringBootTest(
    properties = [
        "laocai.milky.enabled=false"
    ]
)
class ChatModelTest {

    private val log = LoggerFactory.getLogger(javaClass)

    @Resource
    private lateinit var openAiChatModel: OpenAiChatModel

    @Resource
    private var deepSeekChatModel: ChatModel? = null

    @Test
    fun testOpenAiChatModel() {
        val start = System.currentTimeMillis()
        val content = openAiChatModel.call(
            UserMessage.builder()
                .text("这张图里有什么")
                .media(Media.builder()
                    .mimeType(MimeTypeUtils.IMAGE_JPEG)
                    .data(ClassPathResource("nobita.jpg"))
                    .build())
                .build()
        )
        val end = System.currentTimeMillis()
        log.info("Time taken: {} seconds", (end - start) / 1000.0)
        log.info(content)
    }

    @Test
    fun testDeepSeekChatModel() {
        assumeTrue(deepSeekChatModel != null, "当前配置没有启用 DeepSeek")
        val content = deepSeekChatModel!!.call("黑洞是什么")
        log.info(content)
    }
}
