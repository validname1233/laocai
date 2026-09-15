package indi.kyson.laocai.bot.autoconfigure

import indi.kyson.laocai.bot.Bot
import indi.kyson.laocai.bot.MilkyEventSource
import indi.kyson.laocai.bot.application.LaocaiBotRunner
import indi.kyson.laocai.bot.configuration.LaocaiBotConfigurationProperties
import indi.kyson.laocai.bot.listener.EventDispatcher
import indi.kyson.laocai.bot.listener.EventListenerProcessor
import indi.kyson.laocai.bot.listener.EventListenerResolver
import indi.kyson.laocai.bot.listener.EventListenerResolverRegistryProcessor
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpHeaders
import org.springframework.web.reactive.function.client.WebClient

/**
 * Laocai Bot 的 Spring Boot 自动配置入口。
 *
 * 配置了 `laocai.milky.url` 时才装配机器人基础设施；协议处理和事件分发仍由独立运行时类型负责。
 */
@AutoConfiguration
@ConditionalOnProperty(prefix = "laocai.milky", name = ["url"])
@EnableConfigurationProperties(LaocaiBotConfigurationProperties::class)
class LaocaiBotAutoConfiguration {

    @Bean("milkyWebClient")
    @ConditionalOnMissingBean(name = ["milkyWebClient"])
    fun milkyWebClient(
        webClientBuilder: WebClient.Builder,
        properties: LaocaiBotConfigurationProperties,
    ): WebClient {
        val builder = webClientBuilder.baseUrl(properties.milky.url.trimEnd('/') + "/api/")
        val accessToken = properties.milky.accessToken
        if (!accessToken.isNullOrBlank()) {
            builder.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer $accessToken")
        }
        return builder.build()
    }

    @Bean
    @ConditionalOnMissingBean
    fun bot(@Qualifier("milkyWebClient") milkyWebClient: WebClient): Bot = Bot(milkyWebClient)

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "laocai.milky", name = ["enabled"], havingValue = "true", matchIfMissing = true)
    fun milkyEventSource(@Qualifier("milkyWebClient") milkyWebClient: WebClient): MilkyEventSource =
        MilkyEventSource(milkyWebClient)

    @Bean
    @ConditionalOnMissingBean
    fun eventDispatcher(): EventDispatcher = EventDispatcher()

    @Bean
    internal fun eventListenerProcessor(): EventListenerProcessor = EventListenerProcessor()

    @Bean
    @ConditionalOnProperty(prefix = "laocai.milky", name = ["enabled"], havingValue = "true", matchIfMissing = true)
    internal fun laocaiBotRunner(
        eventDispatcher: EventDispatcher,
        milkyEventSource: MilkyEventSource,
        properties: LaocaiBotConfigurationProperties,
        resolvers: List<EventListenerResolver>,
    ): LaocaiBotRunner = LaocaiBotRunner(eventDispatcher, milkyEventSource, properties, resolvers)

    companion object {
        @Bean
        @JvmStatic
        internal fun eventListenerResolverRegistryProcessor(): EventListenerResolverRegistryProcessor =
            EventListenerResolverRegistryProcessor()
    }
}
