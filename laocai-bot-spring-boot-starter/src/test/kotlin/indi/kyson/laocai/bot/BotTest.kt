package indi.kyson.laocai.bot

import indi.kyson.laocai.bot.autoconfigure.LaocaiBotAutoConfiguration
import indi.kyson.laocai.bot.enums.ResponseStatus
import jakarta.annotation.Resource
import org.slf4j.LoggerFactory
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.web.reactive.function.client.WebClient
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@SpringBootTest(
    classes = [
        LaocaiBotAutoConfiguration::class,
        BotTest.TestConfig::class
    ],
    properties = [
        "laocai.milky.enabled=false",
        $$"laocai.milky.url=${MILKY_URL:http://localhost:3010}",
        $$"laocai.milky.access-token=${MILKY_ACCESS_TOKEN:}"
    ]
)
class BotTest {
    
    private val log = LoggerFactory.getLogger(BotTest::class.java)
    
    @TestConfiguration(proxyBeanMethods = false)
    class TestConfig {

        @Bean
        fun webClientBuilder(): WebClient.Builder = WebClient.builder()
    }
    
    @Resource
    private lateinit var bot: Bot
    
    @Test
    fun testGetGroupMemberInfo() {
        val response = bot.getGroupMemberInfo(634550174L, 2331630699L, false).block()

        assertNotNull(response)
        assertEquals(ResponseStatus.OK, response.status)
        assertEquals(0, response.retcode)
        
        val member = response.data?.member
        assertNotNull(member)
        
        log.info("{}", member)
    }
    
}