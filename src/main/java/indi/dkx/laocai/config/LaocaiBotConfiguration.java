package indi.dkx.laocai.config;

import indi.dkx.laocai.core.AutoSseListener;
import indi.dkx.laocai.core.EventDispatcher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Objects;

@Configuration
public class LaocaiBotConfiguration {
    @Bean
    public AutoSseListener autoSseListener(
            EventDispatcher eventDispatcher,
            WebClient.Builder webClientBuilder,
            @Value("${laocai.bot.url:http://localhost:3010}") String botUrl,
            @Value("${laocai.bot.access-token:}") String botToken,
            @Value("${laocai.dispatcher.concurrency:32}") int dispatchConcurrency,
            @Value("${laocai.dispatcher.buffer-size:5000}") int dispatchBufferSize
    ) {
        String baseUrl = Objects.requireNonNull(botUrl, "laocai.bot.url must not be null");
        WebClient webClient = webClientBuilder.baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + botToken)
                .build();
        return new AutoSseListener(eventDispatcher, webClient, dispatchConcurrency, dispatchBufferSize);
    }
}
