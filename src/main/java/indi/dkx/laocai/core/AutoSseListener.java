package indi.dkx.laocai.core;

import indi.dkx.laocai.model.pojo.event.Event;
import indi.dkx.laocai.model.pojo.event.IncomingMessageEvent;
import indi.dkx.laocai.model.pojo.incoming.message.IncomingFriendMessage;
import indi.dkx.laocai.model.pojo.incoming.message.IncomingGroupMessage;
import indi.dkx.laocai.model.pojo.incoming.message.IncomingMessage;
import indi.dkx.laocai.model.pojo.incoming.segment.IncomingTextSegment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.util.retry.Retry;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.codec.ServerSentEvent;

import java.time.Duration;

@Slf4j
@RequiredArgsConstructor
public class AutoSseListener implements CommandLineRunner {

    private final EventDispatcher eventDispatcher;

    private final WebClient.Builder webClientBuilder;

    // 建议把 URL 提取为变量，方便配置
    private final String botUrl;

    @Override
    public void run(String... args) {
        log.info(">>> 准备连接 LLBot SSE...");

        WebClient client = webClientBuilder.baseUrl(botUrl).build();

        // 定义接收类型（推荐用 ServerSentEvent 包装类，比纯 String 更稳）
        ParameterizedTypeReference<ServerSentEvent<Event>> type =
                new ParameterizedTypeReference<>() {};

        client.get()
                .uri("/event")
                .retrieve()
                .bodyToFlux(type)
                // --- 关键：添加重试机制 ---
                .retryWhen(Retry.fixedDelay(Long.MAX_VALUE, Duration.ofSeconds(5))
                        .doBeforeRetry(signal -> {
                            // 关键！打印出具体的异常堆栈
                            log.error("SSE 连接断开或处理失败，5秒后重试。原因: {}", signal.failure().getMessage(), signal.failure());
                        }))
                .subscribe(
                        sse -> {
                            // 处理接收到的数据
                            log.debug("收到事件: {}", sse.data());
                            Event event = sse.data();
                            // 收到消息 -> 扔给调度器
                            if (event != null) eventDispatcher.dispatch(event);
                        },
                        error -> {
                            // 如果重试机制耗尽（上面设置了 MAX_VALUE，理论上不会走到这），才会报错
                            log.error("发生了无法恢复的错误", error);
                        }
        );
    }
}
