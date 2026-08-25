package indi.kyson.laocai.bot.listener

import indi.kyson.laocai.bot.event.Event
import org.slf4j.LoggerFactory
import reactor.core.Disposable
import reactor.core.publisher.BufferOverflowStrategy
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.lang.reflect.InvocationTargetException

/**
 * 事件分发器。
 *
 * 事件处理流程需要一个统一入口把已解析事件推送给所有监听器，而不是让每个监听器自己拉取事件。
 */
class EventDispatcher {

    private val logger = LoggerFactory.getLogger(EventDispatcher::class.java)

    /**
     * 监听器队列。
     *
     * 当前实现按注册顺序分发，先保留顺序语义，后续如果需要排序再替换容器实现。
     */
    private val listenerQueue = mutableListOf<EventListener>()

    /**
     * 注册监听器。
     *
     * 监听器实例由扫描阶段创建，分发器只负责保存和调用，不负责构造。
     */
    internal fun register(listener: EventListener) {
        listenerQueue.add(listener)
    }

    /**
     * 分发事件给所有监听器。
     *
     * 当前阶段还没有全局拦截器，直接顺序调用最容易保证行为可预期。
     * 单个监听器抛出的异常只记录日志、不向上传播，避免一个监听器故障导致后续监听器都无法执行。
     */
    fun dispatch(event: Event) {
        for (listener in listenerQueue) {
            try {
                listener.handle(event)
            } catch (e: Exception) {
                val cause = if (e is InvocationTargetException && e.cause != null) e.cause else e
                logger.error(
                    "监听器 {}#{} 执行异常，已跳过",
                    listener.method.declaringClass.simpleName,
                    listener.method.name,
                    cause,
                )
            }
        }
    }

    /**
     * 订阅一个事件流并持续分发，直到调用方 dispose。
     *
     * 背压和并发度是消费策略，跟事件源怎么产出、怎么重连无关，所以放在这里而不是事件源那侧。
     * 缓冲区超限时丢弃最新事件，避免事件处理拥塞时无限堆积。
     */
    fun consume(events: Flux<Event>, concurrency: Int, bufferSize: Int): Disposable =
        events
            .onBackpressureBuffer(
                bufferSize,
                { dropped -> logger.warn("事件处理拥塞，丢弃最新事件: {}", dropped) },
                BufferOverflowStrategy.DROP_LATEST,
            )
            // 每条事件在 boundedElastic 上执行，避免阻塞 Netty/Reactor 线程
            .flatMap(
                { event -> Mono.fromRunnable<Any> { dispatch(event) }.subscribeOn(Schedulers.boundedElastic()) },
                concurrency,
            )
            .subscribe(
                { },
                { error -> logger.error("发生了无法恢复的错误", error) },
            )
}
