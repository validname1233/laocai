package indi.kyson.laocai.ai.tools

import indi.kyson.laocai.bot.Bot
import org.springframework.ai.chat.model.ToolContext
import org.springframework.ai.tool.annotation.Tool
import org.springframework.ai.tool.annotation.ToolParam
import org.springframework.stereotype.Component
import org.springframework.util.StringUtils
import java.util.Optional

@Component
class BotSenderTool(
    private val bot: Bot,
) {

    @Tool(description = "发送群公告")
    fun sendGroupAnnouncement(
        @ToolParam(description = "要发布的群公告正文") content: String,
        toolContext: ToolContext,
    ): String {
        val groupId = toolContext.context["groupId"] as? Long
            ?: return "失败：未能从上下文中获取 groupId，可能是调用环境未正确注入"
        if (!StringUtils.hasText(content)) {
            return "失败：公告内容为空，请提供非空的 content"
        }

        return try {
            bot.sendGroupAnnouncement(groupId, content, Optional.empty()).block()
            "成功：群公告已发布到群 $groupId"
        } catch (e: Exception) {
            "失败：${e::class.simpleName} - ${e.message}"
        }
    }
}
