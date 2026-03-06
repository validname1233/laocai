package indi.dkx.laocai.bot.listener;

import indi.dkx.laocai.bot.model.event.Event;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Predicate;

/**
 * 事件监听器
 *
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
     * 处理事件
     * @param event 事件
     */
    public void handle(Event<?> event) {
        //  尝试让事件通过匹配器的测试
        if (!matcher.test(event)) return;
        // 事件通过所有匹配器后, 调用方法
        try {
            method.invoke(instance, event);
        } catch (IllegalAccessException | InvocationTargetException e) {
            log.error("调用事件异常", e);
        }
    }
}