package indi.kyson.laocai.app.infrastructure

import org.slf4j.LoggerFactory
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor
import org.springframework.ai.openai.OpenAiChatModel
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.Resource

@Configuration
class ChatClientFactory(
    private val openAiChatModel: OpenAiChatModel,
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @Value("classpath:/prompts/roxy-voice-system-prompt.txt")
    private lateinit var roxyVoiceSystemPrompt: Resource

    fun getRoxyVoiceClient(id: Long): ChatClient {
        log.info("为 id: {} 创建 RoxyVoiceClient 实例", id)
        return ChatClient.builder(openAiChatModel)
            .defaultSystem(roxyVoiceSystemPrompt)
            .defaultAdvisors(SimpleLoggerAdvisor.builder().build())
            .build()
    }
}
