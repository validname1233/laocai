package indi.kyson.laocai.app.conversation

import com.embabel.agent.api.annotation.Action
import com.embabel.agent.api.annotation.Agent
import com.embabel.agent.api.annotation.AchievesGoal
import com.embabel.agent.api.common.MultimodalContent
import com.embabel.agent.api.common.OperationContext
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource

/**
 * The Embabel action is deliberately limited to producing a draft.
 * Sending the draft and recording it are transport/domain side effects owned by
 * [AiConversationService].
 */
@Agent(
    name = "groupReplyAgent",
    description = "Generate a concise, directly sendable QQ group reply from conversation context",
)
class GroupReplyAgent(
    @param:Value("classpath:/prompts/chat-laocai-system-prompt.md")
    private val systemPrompt: Resource,
) {
    @Action(description = "Generate the final group reply from the supplied conversation")
    @AchievesGoal(description = "Produce a directly sendable group reply")
    fun generate(
        context: GroupConversationContext,
        operationContext: OperationContext,
    ): GroupReplyDraft {
        val reply = operationContext
            .ai()
            .withDefaultLlm()
            .withSystemPrompt(systemPrompt.readText())
            .respond(MultimodalContent.withImages(context.text, context.images))
            .content
            .trim()
            .takeIf(String::isNotEmpty)
            ?: error("AI 回复为空")

        return GroupReplyDraft(reply)
    }

    private fun Resource.readText(): String = inputStream.use { it.reader(Charsets.UTF_8).readText() }
}
