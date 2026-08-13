package indi.kyson.laocai.bot.annotation

import indi.kyson.laocai.bot.application.LaocaiBotRunner
import indi.kyson.laocai.bot.configuration.LaocaiBotConfigurationProperties
import indi.kyson.laocai.bot.core.Bot
import indi.kyson.laocai.bot.core.annotation.Listener
import indi.kyson.laocai.bot.core.event.FriendMessageEvent
import indi.kyson.laocai.bot.core.listener.EventDispatcher
import indi.kyson.laocai.bot.core.listener.EventListenerProcessor
import indi.kyson.laocai.bot.core.listener.EventListenerResolver
import indi.kyson.laocai.bot.core.segment.Segment
import indi.kyson.laocai.bot.listener.EventListenerResolverRegistryProcessor
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.FilterType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

import org.assertj.core.api.Assertions.assertThat

class EnableLaocaiBotTests {

    private val contextRunner = ApplicationContextRunner()
        .withPropertyValues(
            "laocai.milky.url=http://localhost:8080",
            "laocai.milky.access-token=test-token",
            "laocai.dispatcher.concurrency=1",
            "laocai.dispatcher.buffer-size=8",
        )

    @Test
    fun enableAnnotationRegistersCompleteBotInfrastructure() {
        contextRunner
            .withUserConfiguration(EnabledConfiguration::class.java)
            .run { context ->
                assertThat(context).hasSingleBean(LaocaiBotConfigurationProperties::class.java)
                    .hasSingleBean(WebClient::class.java)
                    .hasSingleBean(Bot::class.java)
                    .hasSingleBean(EventDispatcher::class.java)
                    .hasSingleBean(EventListenerProcessor::class.java)
                    .hasSingleBean(EventListenerResolverRegistryProcessor::class.java)
                    .hasSingleBean(LaocaiBotRunner::class.java)
                assertThat(context.getBeansOfType(EventListenerResolver::class.java)).hasSize(1)
            }
    }

    @Test
    fun botInfrastructureIsNotScannedWithoutEnableAnnotation() {
        contextRunner
            .withUserConfiguration(WithoutEnableConfiguration::class.java)
            .run { context ->
                assertThat(context).doesNotHaveBean(LaocaiBotConfigurationProperties::class.java)
                    .doesNotHaveBean(WebClient::class.java)
                    .doesNotHaveBean(Bot::class.java)
                    .doesNotHaveBean(EventDispatcher::class.java)
                    .doesNotHaveBean(EventListenerProcessor::class.java)
                    .doesNotHaveBean(EventListenerResolverRegistryProcessor::class.java)
                    .doesNotHaveBean(LaocaiBotRunner::class.java)
                    .doesNotHaveBean(EventListenerResolver::class.java)
            }
    }

    @Configuration(proxyBeanMethods = false)
    @EnableLaocaiBot
    class EnabledConfiguration {

        @Bean
        fun webClientBuilder(): WebClient.Builder = WebClient.builder()

        @Bean
        fun testHandler(bot: Bot): TestHandler = TestHandler(bot)
    }

    @Configuration(proxyBeanMethods = false)
    @ComponentScan(
        basePackages = ["indi.kyson.laocai.bot"],
        includeFilters = [ComponentScan.Filter(type = FilterType.ANNOTATION, classes = [Component::class])],
        excludeFilters = [ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = [EnabledConfiguration::class])],
        useDefaultFilters = false,
    )
    class WithoutEnableConfiguration

    class TestHandler(private val bot: Bot) {

        @Listener
        fun handle(event: FriendMessageEvent) {
            // The method only proves that a consumer Handler can be discovered.
            bot.sendPrivateMsg(event.senderId, emptyList<Segment>())
        }
    }
}
