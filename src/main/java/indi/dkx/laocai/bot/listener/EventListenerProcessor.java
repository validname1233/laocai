package indi.dkx.laocai.bot.listener;

import indi.dkx.laocai.bot.annotation.ApplyBinder;
import indi.dkx.laocai.bot.annotation.Listener;
import indi.dkx.laocai.bot.binder.BinderManager;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Component
public class EventListenerProcessor {

    /**
     *
     * @param beanName 监听器名称
     * @param method 监听器方法
     * @param listenerAnnotation 监听器注解
     * @param applyBinder 绑定器注解
     * @param applicationContext spring上下文
     * @param binderManager 绑定器管理器
     * @return EventListenerResolver
     */
    public EventListenerResolver process(
            String beanName,
            Method method,
            Listener listenerAnnotation,
            ApplyBinder applyBinder,
            ApplicationContext applicationContext,
            BinderManager binderManager
    ) {
        String id = listenerAnnotation.id();
        int priority = listenerAnnotation.priority();
        Class<?> listenTarget = resolveListenTarget(method);

        return dispatcher -> {
            Object instance = applicationContext.getBean(beanName);
            if (!method.canAccess(instance)) {
                method.setAccessible(true);
            }
            var listener = new EventListener(instance, method);
            // TODO
            dispatcher.register(listener);
        };
    }

    // TODO
    private Class<?> resolveListenTarget(Method method) {
        return null;
    }
}
