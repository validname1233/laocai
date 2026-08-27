package indi.kyson.laocai.ai.model

import com.fasterxml.jackson.annotation.JsonPropertyDescription

data class ReplyDecision(
    @get:JsonPropertyDescription("是否应该在当前时刻发言")
    val shouldReply: Boolean,
)
