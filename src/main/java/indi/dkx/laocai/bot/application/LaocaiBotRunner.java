package indi.dkx.laocai.bot.application;

import indi.dkx.laocai.bot.configuration.LaocaiBotConfigurationProperties;
import indi.dkx.laocai.bot.listener.EventDispatcher;
import indi.dkx.laocai.bot.listener.EventListenerResolver;
import indi.dkx.laocai.bot.model.event.Event;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.BufferOverflowStrategy;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.codec.ServerSentEvent;

import java.time.Duration;
import java.util.List;

/**
 * 在 Spring Boot 启动后, 自动执行将该类中的逻辑
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LaocaiBotRunner implements ApplicationRunner {

    /**
     * 事件分发器
     */
    private final EventDispatcher eventDispatcher;

    /**
     * http 客户端 Builder
     */
    private final WebClient.Builder webClientBuilder;

    /**
     * 配置属性
     */
    private final LaocaiBotConfigurationProperties properties;

    /**
     * 事件监听器解析器列表, 通过 EventListenerResolverRegistryProcessor 加载
     */
    private final List<EventListenerResolver> resolvers;

    @Override
    public void run(@NonNull ApplicationArguments args) {
        log.info("检测到 {} 个 EventListenerResolver", resolvers.size());
        resolvers.forEach((EventListenerResolver resolver) ->
                log.debug("EventListenerResolver 实例: {}", resolver.getClass().getName()));
        // 将所有 事件监听器解析器EventListenerResolver 注册到 事件分发器eventDispatcher
        resolvers.forEach((EventListenerResolver resolver) -> resolver.resolve(eventDispatcher));

        // 启动应用
        launchApp();
    }

    /**
     * 启动应用
     */
    private void launchApp() {
        log.info(">>> 准备连接 LLBot SSE...");

        // 定义接收类型（推荐用 ServerSentEvent 包装类，比纯 String 更稳）
        // 创建了一个匿名内部类，这样泛型信息就存进了字节码里
        ParameterizedTypeReference<ServerSentEvent<Event<?>>> type = new ParameterizedTypeReference<>() {};

        webClientBuilder.baseUrl(properties.bot().url())
                .defaultHeader("Authorization", "Bearer " + properties.bot().accessToken())
                .build()
                .get()
                .uri("/event")
                .retrieve()
                .bodyToFlux(type)  //读取字节流，并反序列化
                .mapNotNull(ServerSentEvent::data)
                // 背压：当 handler/下游处理跟不上时，最多缓冲 N 条，超过则丢弃最新的，避免无限堆积
                .onBackpressureBuffer(
                        properties.dispatcher().bufferSize(),
                        dropped -> log.warn("事件处理拥塞，丢弃最新事件: {}", dropped),
                        BufferOverflowStrategy.DROP_LATEST
                )
                // 并发分发：每条事件在 boundedElastic 上执行，避免阻塞 Netty/Reactor 线程
                .flatMap(
                        event -> Mono.fromRunnable(() -> eventDispatcher.dispatch(event))
                                .subscribeOn(Schedulers.boundedElastic()),
                        properties.dispatcher().concurrency()
                )
                // 重试机制
                .retryWhen(Retry.fixedDelay(Long.MAX_VALUE, Duration.ofSeconds(5))
                        .doBeforeRetry((Retry.RetrySignal signal) -> log.error("SSE 连接断开或处理失败，5秒后重试。", signal.failure())))
                .subscribe(
                        (Object ignored) -> { },
                        (Throwable error) -> log.error("发生了无法恢复的错误", error)
                );
    }
}
