package indi.kyson.laocai.bot.configuration

import indi.kyson.laocai.bot.application.LaocaiBotRunner
import indi.kyson.laocai.bot.core.Bot
import indi.kyson.laocai.bot.core.MilkyEventSource
import indi.kyson.laocai.bot.core.listener.EventDispatcher
import indi.kyson.laocai.bot.core.listener.EventListenerProcessor
import indi.kyson.laocai.bot.core.listener.EventListenerResolver
import indi.kyson.laocai.bot.listener.EventListenerResolverRegistryProcessor
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.web.reactive.function.client.WebClient

/**
 * 机器人基础装配配置。
 *
 * 事件分发器、监听器扫描器和运行器属于同一条装配链，集中声明更容易看出依赖关系。
 * 该配置只通过 [EnableLaocaiBot] 显式导入，不参与应用组件扫描。
 */
@EnableConfigurationProperties(LaocaiBotConfigurationProperties::class)
class LaocaiBotConfiguration {

    @Bean("milkyWebClient")
    fun milkyWebClient(
        webClientBuilder: WebClient.Builder,
        properties: LaocaiBotConfigurationProperties,
    ): WebClient =
        webClientBuilder.baseUrl(properties.milky.url)
            .defaultHeader("Authorization", "Bearer ${properties.milky.accessToken}")
            .build()

    @Bean
    fun bot(@Qualifier("milkyWebClient") milkyWebClient: WebClient): Bot = Bot(milkyWebClient)

    @Bean
    fun milkyEventSource(@Qualifier("milkyWebClient") milkyWebClient: WebClient): MilkyEventSource =
        MilkyEventSource(milkyWebClient)

    @Bean
    fun eventDispatcher(): EventDispatcher = EventDispatcher()

    @Bean
    fun eventListenerProcessor(): EventListenerProcessor = EventListenerProcessor()

    @Bean
    fun laocaiBotRunner(
        eventDispatcher: EventDispatcher,
        milkyEventSource: MilkyEventSource,
        properties: LaocaiBotConfigurationProperties,
        resolvers: List<EventListenerResolver>,
    ): LaocaiBotRunner = LaocaiBotRunner(eventDispatcher, milkyEventSource, properties, resolvers)

    companion object {
        @Bean
        @JvmStatic
        fun eventListenerResolverRegistryProcessor(): EventListenerResolverRegistryProcessor =
            EventListenerResolverRegistryProcessor()
    }
}
