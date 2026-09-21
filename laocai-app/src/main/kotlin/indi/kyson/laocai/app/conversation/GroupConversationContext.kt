package indi.kyson.laocai.app.conversation

import com.embabel.agent.api.common.AgentImage

/**
 * All information required to generate one group-chat reply.
 *
 * The text contains the rendered conversation and image markers. The images are
 * kept in the same order as those markers so the model receives one coherent
 * multimodal user message.
 */
data class GroupConversationContext(
    val text: String,
    val images: List<AgentImage> = emptyList(),
)
