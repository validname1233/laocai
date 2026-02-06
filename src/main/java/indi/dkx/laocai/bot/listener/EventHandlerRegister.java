package indi.dkx.laocai.bot.listener;

import indi.dkx.laocai.bot.annotation.Listener;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventHandlerRegister implements ApplicationListener<ContextRefreshedEvent> {

    private final ApplicationContext applicationContext;

    // 内存里存着所有 "订阅者"（用不可变快照发布，保证并发分发时线程安全）
    // volatile 的作用：保证 handlers 这个引用的写入对其它线程立刻可见，并且读到的是一次赋值后的完整新引用（不会读到“半更新”）。
    // List.of() / List.copyOf(found) 的作用：生成的是不可变 List，发布出去后不会再被修改，所以并发读取时不会出现“遍历中被改动”的问题。
    @Getter
    private volatile List<EventListener> eventListeners = List.of();

    // --- 1. 扫描逻辑：Spring 启动完成后自动执行 ---
    @Override
    public void onApplicationEvent(@NonNull ContextRefreshedEvent event) {
        // 防止二次加载
        if (event.getApplicationContext().getParent() != null) return;
        // 获取所有带 @Component 注解的 Bean
        Map<String, Object> beans = applicationContext.getBeansWithAnnotation(Component.class);

        List<EventListener> found = new ArrayList<>();
        // 遍历所有 Bean
        beans.values().forEach(bean -> {
            Method[] methods = bean.getClass().getDeclaredMethods();
            for (Method method : methods) {
                if (method.isAnnotationPresent(Listener.class)) {
                    // 注册这个方法
                    found.add(new EventListener(bean, method));
                    log.debug("注册事件监听器: {}.{}", bean.getClass().getSimpleName(), method.getName());
                }
            }
        });

        this.eventListeners = List.copyOf(found);
    }
}
