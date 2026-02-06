package indi.dkx.laocai.bot.application;

import indi.dkx.laocai.bot.configuration.LaocaiBotConfigurationProperties;
import indi.dkx.laocai.bot.listener.EventDispatcher;
import indi.dkx.laocai.bot.listener.EventListenerResolver;
import indi.dkx.laocai.bot.model.event.Event;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Component
@RequiredArgsConstructor
public class LaocaiBotRunner implements ApplicationRunner {

    private final EventDispatcher eventDispatcher;

    private final WebClient.Builder webClientBuilder;

    private final LaocaiBotConfigurationProperties properties;

    private final List<EventListenerResolver> resolvers;

    @Override
    public void run(ApplicationArguments args) {
        resolvers.forEach(resolver -> resolver.resolve(eventDispatcher));

        launchApp();
    }

    private void launchApp() {
        log.info(">>> 准备连接 LLBot SSE...");

        // 定义接收类型（推荐用 ServerSentEvent 包装类，比纯 String 更稳）
        //创建了一个匿名内部类，这样泛型信息就存进了字节码里
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
                        .doBeforeRetry(signal -> log.error("SSE 连接断开或处理失败，5秒后重试。", signal.failure())))
                .subscribe(
                        ignored -> { },
                        error -> log.error("发生了无法恢复的错误", error)
                );
    }
}
