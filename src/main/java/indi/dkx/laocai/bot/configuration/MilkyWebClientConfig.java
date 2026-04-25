package indi.dkx.laocai.bot.configuration;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@RequiredArgsConstructor
public class MilkyWebClientConfig {

    private final WebClient.Builder webClientBuilder;

    private final LaocaiBotConfigurationProperties properties;

    @Bean
    public WebClient milkyWebClient() {
        return webClientBuilder.baseUrl(properties.bot().url())
                .defaultHeader("Authorization", "Bearer " + properties.bot().accessToken())
                .build();
    }
}
