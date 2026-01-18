package indi.dkx.laocai.core;

import indi.dkx.laocai.annotation.Filter;
import indi.dkx.laocai.annotation.Listener;
import indi.dkx.laocai.model.pojo.event.Event;
import indi.dkx.laocai.model.pojo.event.MessageReceiveEvent;
import indi.dkx.laocai.model.pojo.incoming.message.IncomingMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventDispatcher implements ApplicationListener<ContextRefreshedEvent> {

    private final ApplicationContext applicationContext;

    // 内部类：保存 bean 和 method 的对应关系
    private record HandlerMethod(Object bean, Method method) {}

    // 内存里存着所有 "订阅者"（用不可变快照发布，保证并发分发时线程安全）
    // volatile 的作用：保证 handlers 这个引用的写入对其它线程立刻可见，并且读到的是一次赋值后的完整新引用（不会读到“半更新”）。
    // List.of() / List.copyOf(found) 的作用：生成的是不可变 List，发布出去后不会再被修改，所以并发读取时不会出现“遍历中被改动”的问题。
    private volatile List<HandlerMethod> handlers = List.of();

    // --- 1. 扫描逻辑：Spring 启动完成后自动执行 ---
    @Override
    public void onApplicationEvent(@NonNull ContextRefreshedEvent event) {
        // 防止二次加载
        if (event.getApplicationContext().getParent() != null) return;

        Map<String, Object> beans = applicationContext.getBeansWithAnnotation(Component.class);
        List<HandlerMethod> found = new ArrayList<>();

        beans.values().forEach(bean -> {
            Method[] methods = bean.getClass().getDeclaredMethods();
            for (Method method : methods) {
                if (method.isAnnotationPresent(Listener.class)) {
                    // 注册这个方法
                    found.add(new HandlerMethod(bean, method));
                    log.debug("注册事件监听器: {}.{}", bean.getClass().getSimpleName(), method.getName());
                }
            }
        });

        this.handlers = List.copyOf(found);
    }

    // --- 2. 分发逻辑：由 AutoSseListener 调用 ---
    public void dispatch(Event event) {
        // 先把 volatile 引用读到本地变量，之后循环用的就是这一份快照；
        // 即使同时有线程在刷新 handlers，也只是把引用切到另一份不可变列表，不会影响当前这次遍历。
        List<HandlerMethod> handlersSnapshot = this.handlers;
        for (HandlerMethod handler : handlersSnapshot) {
            try {
                // 1. 获取监听器方法的参数类型（例如 IncomingFriendMessage）
                Class<?> paramType = handler.method.getParameterTypes()[0];

                // 2. 准备要传给方法的参数，默认为原始事件
                Object payload = event;

                // 3. --- 智能拆包逻辑 (你提供的代码) ---
                // 如果当前事件是包装类 (IncomingMessageEvent)，但方法想要的是内部数据 (IncomingMessage及其子类)
                if (event instanceof MessageReceiveEvent messageReceiveEvent) {
                    if (IncomingMessage.class.isAssignableFrom(paramType)) {
                        // 提取内部 data (例如 FriendMessage)
                        payload = messageReceiveEvent.getData();

                        // 执行 @Filter 过滤 (依然使用原始 event 或 payload 进行判断，看 passFilter 怎么写)
                        // 这里建议传 payload 进去判断，或者根据业务需求调整
                        if (!passFilter(handler.method, (IncomingMessage) payload)) {
                            continue;
                        }
                    }
                }

                // 4. 类型检查：确认 payload 真的能塞进这个方法里
                // 比如 payload 是 FriendMessage，方法参数也是 FriendMessage -> 匹配
                // 比如 payload 是 GroupMessage，方法参数是 FriendMessage -> 不匹配，跳过
                if (!paramType.isInstance(payload)) {
                    continue;
                }


                // 6. 反射调用
                handler.method.invoke(handler.bean, payload);

            } catch (Exception e) {
                log.error("事件分发异常", e);
            }
        }
    }

    // --- 辅助：过滤逻辑 ---
    private boolean passFilter(Method method, IncomingMessage incomingMessage) {
        Filter filter = method.getAnnotation(Filter.class);
        if (filter == null || !StringUtils.hasText(filter.value())) {
            return true; // 没有注解或注解为空，直接放行
        }

        String keyword = filter.value();
        String msgContent = incomingMessage.getPlainText(); // 获取消息纯文本

        // 简单的包含关系，可以改成 equals 或正则
        return msgContent != null && msgContent.contains(keyword);
    }
}