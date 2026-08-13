package indi.kyson.laocai.bot.configuration;

import indi.kyson.laocai.bot.application.LaocaiBotRunner;
import indi.kyson.laocai.bot.core.BotSender;
import indi.kyson.laocai.bot.listener.EventDispatcher;
import indi.kyson.laocai.bot.listener.EventListenerProcessor;
import indi.kyson.laocai.bot.listener.EventListenerResolver;
import indi.kyson.laocai.bot.listener.EventListenerResolverRegistryProcessor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

/**
 * 机器人基础装配配置。
 * <p>
 * 事件分发器、监听器扫描器和运行器属于同一条装配链，集中声明更容易看出依赖关系。
 * 该配置只通过 {@code @EnableLaocaiBot} 显式导入，不参与应用组件扫描。
 */
@EnableConfigurationProperties(LaocaiBotConfigurationProperties.class)
public class LaocaiBotConfiguration {

    @Bean(name = "milkyWebClient")
    public WebClient milkyWebClient(
            WebClient.Builder webClientBuilder,
            LaocaiBotConfigurationProperties properties
    ) {
        return webClientBuilder.baseUrl(properties.bot().url())
                .defaultHeader("Authorization", "Bearer " + properties.bot().accessToken())
                .build();
    }

    @Bean
    public BotSender botSender(@Qualifier("milkyWebClient") WebClient milkyWebClient) {
        return new BotSender(milkyWebClient);
    }

    @Bean
    public EventDispatcher eventDispatcher() {
        return new EventDispatcher();
    }

    @Bean
    public EventListenerProcessor eventListenerProcessor() {
        return new EventListenerProcessor();
    }

    @Bean
    public static EventListenerResolverRegistryProcessor eventListenerResolverRegistryProcessor() {
        return new EventListenerResolverRegistryProcessor();
    }

    @Bean
    public LaocaiBotRunner laocaiBotRunner(
            EventDispatcher eventDispatcher,
            @Qualifier("milkyWebClient") WebClient milkyWebClient,
            LaocaiBotConfigurationProperties properties,
            List<EventListenerResolver> resolvers
    ) {
        return new LaocaiBotRunner(eventDispatcher, milkyWebClient, properties, resolvers);
    }
}


