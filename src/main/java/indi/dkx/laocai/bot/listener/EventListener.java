package indi.dkx.laocai.bot.listener;

import indi.dkx.laocai.bot.model.event.Event;
import lombok.extern.slf4j.Slf4j;

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
@Slf4j
public record EventListener(
        Object instance,
        Method method,
        Predicate<Event<?>> matcher
) {
    /**
     * 处理事件。
     * <p>
     * 先做匹配再反射调用，可以避免对不相关事件做无意义的方法执行。
     * @param event 事件
     */
    public void handle(Event<?> event) {
        if (!matcher.test(event)) return;
        try {
            method.invoke(instance, event);
        } catch (IllegalAccessException | InvocationTargetException e) {
            log.error("调用事件异常", e);
        }
    }
}

