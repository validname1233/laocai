package indi.kyson.laocai.bot.application

import indi.kyson.laocai.bot.configuration.LaocaiBotConfigurationProperties
import indi.kyson.laocai.bot.MilkyEventSource
import indi.kyson.laocai.bot.listener.EventDispatcher
import indi.kyson.laocai.bot.listener.EventListenerResolver
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner

/**
 * 在 Spring Boot 启动后，自动执行将该类中的逻辑。
 */
internal class LaocaiBotRunner(
    private val eventDispatcher: EventDispatcher,
    private val milkyEventSource: MilkyEventSource,
    private val properties: LaocaiBotConfigurationProperties,
    private val resolvers: List<EventListenerResolver>,
) : ApplicationRunner {

    private val logger = LoggerFactory.getLogger(LaocaiBotRunner::class.java)

    override fun run(args: ApplicationArguments) {
        logger.info("检测到 {} 个 EventListenerResolver", resolvers.size)
        resolvers.forEach { resolver -> logger.debug("EventListenerResolver 实例: {}", resolver.javaClass.name) }
        // 将所有 事件监听器解析器EventListenerResolver 注册到 事件分发器eventDispatcher
        resolvers.forEach { resolver -> resolver.resolve(eventDispatcher) }

        eventDispatcher.consume(
            milkyEventSource.eventFlux(),
            properties.dispatcher.concurrency,
            properties.dispatcher.bufferSize,
        )
    }
}
