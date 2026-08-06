package indi.dkx.laocai.bot.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "laocai")
public record LaocaiBotConfigurationProperties(
        @DefaultValue Bot bot,
        @DefaultValue Dispatcher dispatcher
) {

    /**
     * 机器人远端连接配置。
     * <p>
     * bot 的 URL 和 access token 属于同一组外部连接参数，应该作为一个原子配置块读取。
     */
    public record Bot(
            String url,
            String accessToken
    ) {}

    /**
     * 事件分发配置。
     * <p>
     * 并发度和缓冲上限会一起影响 SSE 消费的稳定性，放在同一个配置块里更容易理解。
     * @param concurrency 事件分发并发度
     * @param bufferSize 事件积压缓冲上限
     */
    public record Dispatcher(
            Integer concurrency,
            Integer bufferSize
    ) {}
}


