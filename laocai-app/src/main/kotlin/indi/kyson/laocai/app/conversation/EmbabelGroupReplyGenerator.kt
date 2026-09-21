package indi.kyson.laocai.app.conversation

import com.embabel.agent.api.invocation.AgentInvocation
import com.embabel.agent.core.AgentPlatform
import org.springframework.stereotype.Component

/** Adapts the synchronous application boundary to Embabel's typed agent invocation API. */
@Component
class EmbabelGroupReplyGenerator(
    private val agentPlatform: AgentPlatform,
) : GroupReplyGenerator {
    override fun generate(context: GroupConversationContext): String =
        AgentInvocation
            .create(agentPlatform, GroupReplyDraft::class.java)
            .invoke(context)
            .reply
            .trim()
            .takeIf(String::isNotEmpty)
            ?: error("AI 回复为空")
}
