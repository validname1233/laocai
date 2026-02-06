package indi.dkx.laocai.bot.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "laocai")
public record LaocaiBotConfigurationProperties(
        @DefaultValue Bot bot,               // @DefaultValue 确保如果yaml没配，给予默认空对象而非null
        @DefaultValue Dispatcher dispatcher
) {

    public record Bot(
            String url,
            String accessToken
    ) {}

    /**
     *
     * @param concurrency  事件分发并发度（避免 handler 阻塞拖垮 SSE 消费线程）
     * @param bufferSize 事件积压缓冲上限（超过上限按策略丢弃，避免 OOM）
     */
    public record Dispatcher(
            Integer concurrency,
            Integer bufferSize
    ) {}
}
