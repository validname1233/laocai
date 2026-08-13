package indi.kyson.laocai.bot.listener;

import indi.kyson.laocai.bot.model.event.Event;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Predicate;

/**
 * 事件监听器。
 * <p>
 * 把目标实例、方法和匹配条件打包成一个轻量对象，分发器就只需要负责调用。
 * @param instance 监听器实例
 * @param method 监听器方法
 * @param matcher 匹配器
 */
public record EventListener(
        Object instance,
        Method method,
        Predicate<Event<?>> matcher
) {
    /**
     * 处理事件。
     * <p>
     * 先做匹配再反射调用；匹配器和反射调用产生的异常都不在这里吞掉，
     * 统一交给分发器兜底，避免异常处理逻辑分散在两处。
     * @param event 事件
     */
    public void handle(Event<?> event) throws IllegalAccessException, InvocationTargetException {
        if (!matcher.test(event)) return;
        method.invoke(instance, event);
    }
}

