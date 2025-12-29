package indi.dkx.laocai.core;

import indi.dkx.laocai.model.pojo.event.Event;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.BufferOverflowStrategy;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.codec.ServerSentEvent;

import java.time.Duration;
import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
public class AutoSseListener implements CommandLineRunner {

    private final EventDispatcher eventDispatcher;

    private final WebClient webClient;

    /**
     * 事件分发并发度（避免 handler 阻塞拖垮 SSE 消费线程）
     */
    private final int dispatchConcurrency;

    /**
     * 事件积压缓冲上限（超过上限按策略丢弃，避免 OOM）
     */
    private final int dispatchBufferSize;

    @Override
    public void run(String... args) {
        log.info(">>> 准备连接 LLBot SSE...");

        // 定义接收类型（推荐用 ServerSentEvent 包装类，比纯 String 更稳）
        ParameterizedTypeReference<ServerSentEvent<Event>> type = new ParameterizedTypeReference<>() {};

        webClient.get()
                .uri("/event")
                .retrieve()
                .bodyToFlux(type)
                .mapNotNull(ServerSentEvent::data)
                // 背压：当 handler/下游处理跟不上时，最多缓冲 N 条，超过则丢弃最旧的，避免无限堆积
                .onBackpressureBuffer(
                        dispatchBufferSize,
                        dropped -> log.warn("事件处理拥塞，丢弃最旧事件: {}", dropped),
                        BufferOverflowStrategy.DROP_LATEST
                )
                // 并发分发：每条事件在 boundedElastic 上执行，避免阻塞 Netty/Reactor 线程
                .flatMap(
                        event -> Mono.fromRunnable(() -> eventDispatcher.dispatch(event))
                                .subscribeOn(Schedulers.boundedElastic()),
                        dispatchConcurrency
                )
                // --- 关键：添加重试机制 ---
                .retryWhen(Retry.fixedDelay(Long.MAX_VALUE, Duration.ofSeconds(5))
                        .doBeforeRetry(signal -> log.error("SSE 连接断开或处理失败，5秒后重试。", signal.failure())))
                .subscribe(
                        ignored -> { },
                        error -> log.error("发生了无法恢复的错误", error)
                );
    }
}
