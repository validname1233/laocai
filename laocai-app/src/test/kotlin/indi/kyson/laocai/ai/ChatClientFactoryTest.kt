package indi.kyson.laocai.ai

import jakarta.annotation.Resource
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class ChatClientFactoryTest {

    @Resource
    private lateinit var chatClientFactory: ChatClientFactory

    @Test
    fun testImage() {
        // Placeholder for image test
    }
}
