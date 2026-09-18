package indi.kyson.laocai.ai

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

    @Value("classpath:/prompts/chat-laocai-system-prompt.md")
    private lateinit var chatLaocaiSystemPrompt: Resource

    @Value("classpath:/prompts/roxy-voice-system-prompt.txt")
    private lateinit var roxyVoiceSystemPrompt: Resource

    fun getChatClient(id: Long): ChatClient {
        log.info("为 id: {} 创建 ChatClient 实例", id)
        return ChatClient.builder(openAiChatModel).build()
    }

    fun getChatLaocaiClient(id: Long): ChatClient {
        log.info("为 id: {} 创建 ChatLaocaiClient 实例", id)
        return ChatClient.builder(openAiChatModel)
            .defaultSystem(chatLaocaiSystemPrompt)
            .defaultAdvisors(SimpleLoggerAdvisor.builder().build())
            .build()
    }

    fun getRoxyVoiceClient(id: Long): ChatClient {
        log.info("为 id: {} 创建 RoxyVoiceClient 实例", id)
        return ChatClient.builder(openAiChatModel)
            .defaultSystem(roxyVoiceSystemPrompt)
            .defaultAdvisors(SimpleLoggerAdvisor.builder().build())
            .build()
    }
}
