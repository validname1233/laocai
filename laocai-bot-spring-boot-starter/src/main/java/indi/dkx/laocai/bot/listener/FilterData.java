package indi.dkx.laocai.bot.listener;

import indi.dkx.laocai.bot.model.event.Event;

import java.util.function.Predicate;

/**
 * 过滤规则和其优先级的组合。
 * <p>
 * 监听器扫描阶段会把注解转换成统一的 matcher 结构，后面只需要按优先级组合即可。
 * @param priority 过滤优先级
 * @param matcher 事件匹配器
 */
public record FilterData(
        int priority,
        Predicate<Event<?>> matcher
) {
}
