package indi.kyson.laocai.bot.autoconfigure

import indi.kyson.laocai.bot.annotation.Listener
import indi.kyson.laocai.bot.entity.FriendCategoryEntity
import indi.kyson.laocai.bot.entity.FriendEntity
import indi.kyson.laocai.bot.enums.Sex
import indi.kyson.laocai.bot.event.FriendMessageEvent
import indi.kyson.laocai.bot.listener.EventDispatcher
import indi.kyson.laocai.bot.listener.EventListenerResolver
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.CopyOnWriteArrayList

class ListenerPriorityIntegrationTest {

    private val contextRunner = ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(LaocaiBotAutoConfiguration::class.java))
        .withUserConfiguration(
            LaocaiBotAutoConfigurationTests.WebClientConfiguration::class.java,
            PriorityListenerConfiguration::class.java,
        )

    @Test
    fun listenerPriorityIsResolvedFromDirectAndComposedAnnotations() {
        contextRunner
            .withPropertyValues("laocai.milky.url=http://localhost:8080")
            .run { context ->
                val dispatcher = context.getBean(EventDispatcher::class.java)
                context.getBeansOfType(EventListenerResolver::class.java).values
                    .forEach { resolver -> resolver.resolve(dispatcher) }
                dispatcher.sortListeners()

                dispatcher.dispatch(sampleEvent())

                assertThat(context.getBean(PriorityHandler::class.java).calls)
                    .containsExactly("high", "default", "low")
            }
    }

    @Target(AnnotationTarget.FUNCTION)
    @Retention(AnnotationRetention.RUNTIME)
    @Listener(priority = -100)
    private annotation class HighPriorityListener

    @Configuration(proxyBeanMethods = false)
    class PriorityListenerConfiguration {

        @Bean
        fun priorityHandler(): PriorityHandler = PriorityHandler()
    }

    class PriorityHandler {
        val calls = CopyOnWriteArrayList<String>()

        @Listener(priority = 100)
        fun low(event: FriendMessageEvent) {
            calls.add("low")
        }

        @Listener
        fun default(event: FriendMessageEvent) {
            calls.add("default")
        }

        @HighPriorityListener
        fun high(event: FriendMessageEvent) {
            calls.add("high")
        }
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
}