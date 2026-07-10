package indi.dkx.laocai.bot.configuration;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Milky WebClient 配置。
 * <p>
 * 所有对外请求都要共享同一 baseUrl 和鉴权方式，集中配置可以避免重复拼接和漏配。
 */
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(LaocaiBotConfigurationProperties.class)
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


