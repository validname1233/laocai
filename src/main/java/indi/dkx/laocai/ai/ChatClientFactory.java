package indi.dkx.laocai.ai;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.redis.RedisChatMemoryRepository;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

/**
 * ChatClient 创建工厂
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class ChatClientFactory {

    private final ChatModel chatModel;

    private final RedisChatMemoryRepository redisChatMemoryRepository;

    @Value("classpath:/prompt/chat-laocai-system-prompt.txt")
    private Resource chatLaocaiSystemPrompt;

    /**
     * 根据 id 获取 ChatClient 实例
     *
     * @param id 会话 id
     * @return ChatClient 实例
     */
    public ChatClient getChatClient(long id) {
        log.info("为 id: {} 创建 ChatClient 实例", id);
        // 根据 id 创建独立的对话记忆
        MessageChatMemoryAdvisor messageChatMemoryAdvisor = MessageChatMemoryAdvisor
                .builder(
                        MessageWindowChatMemory.builder()
                        .chatMemoryRepository(redisChatMemoryRepository)
                        .maxMessages(10)
                        .build()
                )
                .conversationId(String.valueOf(id))
                .build();
        // TODO: 从群聊加载最新的聊天记录到对话记忆

        // TODO: 根据任务类型创建不同的 ChatClient
        return ChatClient.builder(chatModel)
                .defaultSystem(chatLaocaiSystemPrompt)
                // TODO: 添加工具
//                .defaultTools(new WeatherTool())
                .defaultAdvisors(
                        messageChatMemoryAdvisor,
                        SimpleLoggerAdvisor.builder().build()
                ).build();
    }
}
