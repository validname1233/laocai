package indi.dkx.laocai.ai.model;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

/**
 * AI 是否应该回复的判断结果。
 * <p>
 * 先做决策，再做发言生成，可以把是否开口和具体说什么拆成两步。
 * @param shouldReply 是否应该在当前时刻发言
 */
public record ReplyDecision(
        @JsonPropertyDescription("是否应该在当前时刻发言")
        boolean shouldReply
) {
}


