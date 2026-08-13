package indi.kyson.laocai.bot.core.listener

import indi.kyson.laocai.bot.core.event.Event

/**
 * 过滤规则和其优先级的组合。
 *
 * 监听器扫描阶段会把注解转换成统一的 matcher 结构，后面只需要按优先级组合即可。
 */
data class FilterData(
    val priority: Int,
    val matcher: (Event) -> Boolean,
)
