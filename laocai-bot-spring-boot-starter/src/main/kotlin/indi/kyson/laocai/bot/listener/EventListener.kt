package indi.kyson.laocai.bot.listener

import indi.kyson.laocai.bot.event.Event
import java.lang.reflect.Method

/**
 * 事件监听器。
 *
 * 把目标实例、方法和匹配条件打包成一个轻量对象，分发器就只需要负责调用。
 */
internal data class EventListener(
    val instance: Any,
    val method: Method,
    val matcher: (Event) -> Boolean,
) {
    /**
     * 处理事件。
     *
     * 先做匹配再反射调用；匹配器和反射调用产生的异常都不在这里吞掉，
     * 统一交给分发器兜底，避免异常处理逻辑分散在两处。
     */
    fun handle(event: Event) {
        if (!matcher(event)) return
        method.invoke(instance, event)
    }
}
