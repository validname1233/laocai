package indi.dkx.laocai.ai.tools;

import indi.dkx.laocai.bot.core.BotSender;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class BotSenderTool {

    private final BotSender botSender;

    /**
     * 发送群公告。
     * <p>
     * Tool 层只做最薄的上下文适配，不把消息发送规则重复写到 AI 侧。
     */
    @Tool(description = "发送群公告")
    public String sendGroupAnnouncement(
            @ToolParam(description = "要发布的群公告正文") String content,
            ToolContext toolContext) {

        Long groupId = (Long) toolContext.getContext().get("groupId");
        if (groupId == null) return "失败：未能从上下文中获取 groupId，可能是调用环境未正确注入";
        if (!StringUtils.hasText(content)) return "失败：公告内容为空，请提供非空的 content";

        try {
            botSender.sendGroupAnnouncement(groupId, content, Optional.empty()).block();
            return "成功：群公告已发布到群 " + groupId;
        } catch (Exception e) {
            return "失败：" + e.getClass().getSimpleName() + " - " + e.getMessage();
        }
    }
}


