package indi.dkx.laocai.ai;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

/**
 * ChatClient 创建工厂。
 * <p>
 * 不同任务需要不同 system prompt 和观察器配置，把差异收敛到工厂里更容易维护。
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class ChatClientFactory {

    private final OpenAiChatModel openAiChatModel;

    /**
     * 发送对话系统提示词。
     * <p>
     * 人格设定是会话级上下文，应该从资源文件加载，而不是散落在业务代码里。
     */
    @Value("classpath:/prompt/chat-persona-system-prompt.txt")
    private Resource chatPersonaSystemPrompt;

    /**
     * 判断是否回复系统提示词。
     * <p>
     * 是否开口和怎么开口是两步不同的决策，需要各自独立的提示词。
     */
    @Value("classpath:/prompt/reply-decision-system-prompt.txt")
    private Resource replyDecisionSystemPrompt;

    /**
     * 洛琪希语音人格系统提示词。
     * <p>
     * /audio 场景要走语音合成，文字回复必须只用日语，且要短，这和普通聊天人格的约束不一样，需要单独提示词。
     */
    @Value("classpath:/prompt/roxy-voice-system-prompt.txt")
    private Resource roxyVoiceSystemPrompt;

    public ChatClient getChatClient(long id) {
        log.info("为 id: {} 创建 ChatClient 实例", id);
        return ChatClient.builder(openAiChatModel)
                .build();
    }

    /**
     * 根据 id 获取发送对话 ChatClient 实例。
     * <p>
     * 人格对话需要固定的 system prompt 和日志观察器，不能和其它用途共用同一配置。
     * @param id 会话 id
     * @return 发送对话 ChatClient 实例
     */
    public ChatClient getChatPersonaClient(long id) {
        log.info("为 id: {} 创建 ChatPersonaClient 实例", id);
        return ChatClient.builder(openAiChatModel)
                .defaultSystem(chatPersonaSystemPrompt)
                .defaultAdvisors(
                        SimpleLoggerAdvisor.builder().build()
                ).build();
    }

    /**
     * 根据 id 获取判断是否回复 ChatClient 实例。
     * <p>
     * 先判断是否发言，再生成具体回复，可以把决策和生成拆开，避免无意义的回复调用。
     * @param id 会话 id
     * @return 判断是否回复 ChatClient 实例
     */
    public ChatClient getReplyDecisionClient(long id) {
        log.info("为 id: {} 创建回复判断 ChatClient 实例", id);
        return ChatClient.builder(openAiChatModel)
                .defaultSystem(replyDecisionSystemPrompt)
                .defaultAdvisors(
                        SimpleLoggerAdvisor.builder().build()
                ).build();
    }

    /**
     * 根据 id 获取洛琪希语音人格 ChatClient 实例。
     * <p>
     * 这个回复要交给日语 TTS 朗读，所以输出语言和长度都有硬约束，不能复用普通群聊人格。
     * @param id 会话 id
     * @return 洛琪希语音人格 ChatClient 实例
     */
    public ChatClient getRoxyVoiceClient(long id) {
        log.info("为 id: {} 创建 RoxyVoiceClient 实例", id);
        return ChatClient.builder(openAiChatModel)
                .defaultSystem(roxyVoiceSystemPrompt)
                .defaultAdvisors(
                        SimpleLoggerAdvisor.builder().build()
                ).build();
    }
}
