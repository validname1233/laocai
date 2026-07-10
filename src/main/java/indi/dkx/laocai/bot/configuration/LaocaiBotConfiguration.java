package indi.dkx.laocai.bot.configuration;

import indi.dkx.laocai.bot.application.LaocaiBotRunner;
import indi.dkx.laocai.bot.listener.EventDispatcher;
import indi.dkx.laocai.bot.listener.EventListenerProcessor;
import indi.dkx.laocai.bot.listener.EventListenerResolver;
import indi.dkx.laocai.bot.listener.EventListenerResolverRegistryProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

/**
 * 机器人基础装配配置。
 * <p>
 * 事件分发器、监听器扫描器和运行器属于同一条装配链，集中声明更容易看出依赖关系。
 */
public class LaocaiBotConfiguration {

    @Bean
    public EventDispatcher eventDispatcher() {
        return new EventDispatcher();
    }

    @Bean
    public EventListenerProcessor eventListenerProcessor() {
        return new EventListenerProcessor();
    }

    @Bean
    public EventListenerResolverRegistryProcessor eventListenerResolverRegistryProcessor() {
        return new EventListenerResolverRegistryProcessor();
    }

    @Bean
    public LaocaiBotRunner laocaiBotRunner(
            EventDispatcher eventDispatcher,
            WebClient.Builder webClientBuilder,
            LaocaiBotConfigurationProperties properties,
            List<EventListenerResolver> resolvers
    ) {
        return new LaocaiBotRunner(eventDispatcher, webClientBuilder, properties, resolvers);
    }
}


