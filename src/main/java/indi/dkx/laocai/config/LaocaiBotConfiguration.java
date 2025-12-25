package indi.dkx.laocai.config;

import indi.dkx.laocai.core.AutoSseListener;
import indi.dkx.laocai.core.EventDispatcher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class LaocaiBotConfiguration {
    @Bean
    public AutoSseListener autoSseListener(
            EventDispatcher eventDispatcher,
            WebClient.Builder webClientBuilder,
            @Value("${laocai.bot.url:http://localhost:3010}") String botUrl
    ) {
        return new AutoSseListener(eventDispatcher, webClientBuilder, botUrl);
    }
}
