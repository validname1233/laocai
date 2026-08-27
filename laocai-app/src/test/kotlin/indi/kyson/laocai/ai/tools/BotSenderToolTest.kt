package indi.kyson.laocai.ai.tools

import indi.kyson.laocai.ai.ChatClientFactory
import jakarta.annotation.Resource
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class BotSenderToolTest {

    @Resource
    private lateinit var chatClientFactory: ChatClientFactory

    @Resource
    private lateinit var botSenderTool: BotSenderTool

    @Test
    fun sendGroupAnnouncement() {
        chatClientFactory.getChatPersonaClient(1098197034L)
            .prompt()
            .tools(botSenderTool)
            .toolContext(mapOf("groupId" to 1098197034L))
            .user("牢财能帮我发一个群公告吗，内容你自己想")
            .call()
            .content()
    }
}
