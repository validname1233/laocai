package indi.kyson.laocai.bot.listener

/**
 * 事件监听器解析器。
 *
 * 扫描阶段只需要一个延迟注册入口，真正实例化和绑定动作放到容器就绪后再执行。
 */
internal fun interface EventListenerResolver {
    fun resolve(dispatcher: EventDispatcher)
}
