package indi.kyson.laocai.bot.core

import indi.kyson.laocai.bot.core.event.Event
import org.slf4j.LoggerFactory
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.codec.ServerSentEvent
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux
import reactor.util.retry.Retry
import java.time.Duration
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Milky 入站事件源。
 *
 * 只负责连接 /event、反序列化、断线重连，不管下游怎么消费——
 * 背压和并发分发策略是 EventDispatcher 的事，两者的变化原因不同，分开才不会互相牵连。
 */
class MilkyEventSource(private val milkyWebClient: WebClient) {

    private val logger = LoggerFactory.getLogger(MilkyEventSource::class.java)

    /**
     * 事件流。
     *
     * 断线后无限重试、固定 5 秒间隔重连，因此这条流本身不会以 onError/onComplete 终止。
     */
    fun eventFlux(): Flux<Event> {
        val connectedLogged = AtomicBoolean(false)
        val type = object : ParameterizedTypeReference<ServerSentEvent<Event>>() {}

        return milkyWebClient
            .get()
            .uri("/event")
            .retrieve()
            .bodyToFlux(type)
            .mapNotNull { it.data() }
            .doOnSubscribe { logger.info("准备连接 LLBot") }
            .doOnNext {
                if (connectedLogged.compareAndSet(false, true)) {
                    logger.info("LLBot 连接成功")
                }
            }
            .retryWhen(
                Retry.fixedDelay(Long.MAX_VALUE, Duration.ofSeconds(5))
                    .doBeforeRetry { signal ->
                        connectedLogged.set(false)
                        logger.error("SSE 连接断开或处理失败，5秒后重试。", signal.failure())
                    },
            )
    }
}
