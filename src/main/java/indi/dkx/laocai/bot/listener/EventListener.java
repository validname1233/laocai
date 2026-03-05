package indi.dkx.laocai.bot.listener;

import indi.dkx.laocai.bot.model.event.Event;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Predicate;

@Slf4j
public record EventListener(
        Object instance,
        Method method,
        Predicate<Event<?>> matcher
) {
    // 可以在这里封装 invoke 逻辑
    public void handle(Event<?> event) {
        if (!matcher.test(event)) return;
        try {
            method.invoke(instance, event);
        } catch (IllegalAccessException | InvocationTargetException e) {
            log.error("调用事件异常", e);
        }
    }
}