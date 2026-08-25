package indi.kyson.laocai.bot.autoconfigure

import indi.kyson.laocai.bot.Bot
import indi.kyson.laocai.bot.MilkyEventSource
import indi.kyson.laocai.bot.annotation.Listener
import indi.kyson.laocai.bot.application.LaocaiBotRunner
import indi.kyson.laocai.bot.configuration.LaocaiBotConfigurationProperties
import indi.kyson.laocai.bot.entity.FriendCategoryEntity
import indi.kyson.laocai.bot.entity.FriendEntity
import indi.kyson.laocai.bot.enums.Sex
import indi.kyson.laocai.bot.event.FriendMessageEvent
import indi.kyson.laocai.bot.listener.EventDispatcher
import indi.kyson.laocai.bot.listener.EventListenerProcessor
import indi.kyson.laocai.bot.listener.EventListenerResolver
import indi.kyson.laocai.bot.listener.EventListenerResolverRegistryProcessor
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.test.context.assertj.AssertableApplicationContext
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference

class LaocaiBotAutoConfigurationTests {

    private val contextRunner = ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(LaocaiBotAutoConfiguration::class.java))
        .withUserConfiguration(WebClientConfiguration::class.java)

    @Test
    fun missingUrlDoesNotActivateBotInfrastructure() {
        contextRunner.run { context ->
            assertThat(context).doesNotHaveBean(LaocaiBotConfigurationProperties::class.java)
                .doesNotHaveBean("milkyWebClient")
                .doesNotHaveBean(Bot::class.java)
                .doesNotHaveBean(MilkyEventSource::class.java)
                .doesNotHaveBean(EventDispatcher::class.java)
                .doesNotHaveBean(EventListenerProcessor::class.java)
                .doesNotHaveBean(EventListenerResolverRegistryProcessor::class.java)
                .doesNotHaveBean(LaocaiBotRunner::class.java)
                .doesNotHaveBean(EventListenerResolver::class.java)
        }
    }

    @Test
    fun urlWithoutTokenCreatesInfrastructureWithoutAuthorizationHeader() {
        contextRunner
            .withPropertyValues("laocai.milky.url=http://localhost:8080")
            .run { context ->
                assertCompleteInfrastructure(context)

                val webClient = context.getBean("milkyWebClient", WebClient::class.java)
                webClient.get().uri("probe").retrieve().toBodilessEntity().block()

                val request = context.getBean(RequestRecorder::class.java).request.get()
                assertThat(request.headers().getFirst(HttpHeaders.AUTHORIZATION)).isNull()
            }
    }

    @Test
    fun configuredTokenIsSentAsBearerAuthorizationHeader() {
        contextRunner
            .withPropertyValues(
                "laocai.milky.url=http://localhost:8080",
                "laocai.milky.access-token=test-token",
                "laocai.dispatcher.concurrency=1",
                "laocai.dispatcher.buffer-size=8",
            )
            .run { context ->
                assertCompleteInfrastructure(context)

                val webClient = context.getBean("milkyWebClient", WebClient::class.java)
                webClient.get().uri("probe").retrieve().toBodilessEntity().block()

                val request = context.getBean(RequestRecorder::class.java).request.get()
                assertThat(request.headers().getFirst(HttpHeaders.AUTHORIZATION)).isEqualTo("Bearer test-token")
            }
    }

    @Test
    fun userProvidedRuntimeBeansReplaceDefaults() {
        contextRunner
            .withUserConfiguration(CustomRuntimeConfiguration::class.java)
            .withPropertyValues("laocai.milky.url=http://localhost:8080")
            .run { context ->
                assertThat(context).hasBean("milkyWebClient")
                    .hasBean("customBot")
                    .doesNotHaveBean("bot")
                    .hasBean("customMilkyEventSource")
                    .doesNotHaveBean("milkyEventSource")
                    .hasBean("customEventDispatcher")
                    .doesNotHaveBean("eventDispatcher")
                assertThat(context).hasSingleBean(Bot::class.java)
                    .hasSingleBean(MilkyEventSource::class.java)
                    .hasSingleBean(EventDispatcher::class.java)
            }
    }

    @Test
    fun listenerMethodIsResolvedAndDispatched() {
        contextRunner
            .withUserConfiguration(ListenerConfiguration::class.java)
            .withPropertyValues("laocai.milky.url=http://localhost:8080")
            .run { context ->
                val resolvers = context.getBeansOfType(EventListenerResolver::class.java).values
                assertThat(resolvers).hasSize(1)

                val dispatcher = context.getBean(EventDispatcher::class.java)
                resolvers.forEach { resolver -> resolver.resolve(dispatcher) }
                dispatcher.dispatch(sampleEvent())

                assertThat(context.getBean(TestHandler::class.java).handledCount.get()).isEqualTo(1)
            }
    }

    private fun assertCompleteInfrastructure(context: AssertableApplicationContext) {
        assertThat(context).hasSingleBean(LaocaiBotConfigurationProperties::class.java)
            .hasBean("milkyWebClient")
            .hasSingleBean(Bot::class.java)
            .hasSingleBean(MilkyEventSource::class.java)
            .hasSingleBean(EventDispatcher::class.java)
            .hasSingleBean(EventListenerProcessor::class.java)
            .hasSingleBean(EventListenerResolverRegistryProcessor::class.java)
            .hasSingleBean(LaocaiBotRunner::class.java)
    }

    private fun sampleEvent() = FriendMessageEvent(
        time = 0,
        selfId = 1,
        peerId = 2,
        messageSeq = 3,
        senderId = 2,
        segments = emptyList(),
        friend = FriendEntity(2, "friend", Sex.UNKNOWN, "", "", FriendCategoryEntity(0, "default")),
    )

    class RequestRecorder {
        val request = AtomicReference<ClientRequest>()
    }

    @Configuration(proxyBeanMethods = false)
    class WebClientConfiguration {

        @Bean
        fun requestRecorder(): RequestRecorder = RequestRecorder()

        @Bean
        fun webClientBuilder(recorder: RequestRecorder): WebClient.Builder =
            WebClient.builder().exchangeFunction { request ->
                recorder.request.set(request)
                Mono.just(ClientResponse.create(HttpStatus.OK).body("ok").build())
            }
    }

    @Configuration(proxyBeanMethods = false)
    class CustomRuntimeConfiguration {

        @Bean("milkyWebClient")
        fun milkyWebClient(webClientBuilder: WebClient.Builder): WebClient = webClientBuilder.build()

        @Bean("customBot")
        fun customBot(@Qualifier("milkyWebClient") webClient: WebClient): Bot = Bot(webClient)

        @Bean("customMilkyEventSource")
        fun customMilkyEventSource(@Qualifier("milkyWebClient") webClient: WebClient): MilkyEventSource =
            MilkyEventSource(webClient)

        @Bean("customEventDispatcher")
        fun customEventDispatcher(): EventDispatcher = EventDispatcher()
    }

    @Configuration(proxyBeanMethods = false)
    class ListenerConfiguration {

        @Bean
        fun testHandler(): TestHandler = TestHandler()
    }

    class TestHandler {
        val handledCount = AtomicInteger()

        @Listener
        fun handle(event: FriendMessageEvent) {
            handledCount.incrementAndGet()
        }
    }
}
