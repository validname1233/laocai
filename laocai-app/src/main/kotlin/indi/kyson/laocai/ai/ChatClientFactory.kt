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

    @Value("classpath:/prompt/chat-persona-system-prompt.txt")
    private lateinit var chatPersonaSystemPrompt: Resource

    @Value("classpath:/prompt/reply-decision-system-prompt.txt")
    private lateinit var replyDecisionSystemPrompt: Resource

    @Value("classpath:/prompt/roxy-voice-system-prompt.txt")
    private lateinit var roxyVoiceSystemPrompt: Resource

    fun getChatClient(id: Long): ChatClient {
        log.info("为 id: {} 创建 ChatClient 实例", id)
        return ChatClient.builder(openAiChatModel).build()
    }

    fun getChatPersonaClient(id: Long): ChatClient {
        log.info("为 id: {} 创建 ChatPersonaClient 实例", id)
        return ChatClient.builder(openAiChatModel)
            .defaultSystem(chatPersonaSystemPrompt)
            .defaultAdvisors(SimpleLoggerAdvisor.builder().build())
            .build()
    }

    fun getReplyDecisionClient(id: Long): ChatClient {
        log.info("为 id: {} 创建回复判断 ChatClient 实例", id)
        return ChatClient.builder(openAiChatModel)
            .defaultSystem(replyDecisionSystemPrompt)
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
