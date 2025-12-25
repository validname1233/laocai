package indi.dkx.laocai.core;

import indi.dkx.laocai.annotation.Filter;
import indi.dkx.laocai.annotation.Listener;
import indi.dkx.laocai.model.pojo.event.Event;
import indi.dkx.laocai.model.pojo.event.IncomingMessageEvent;
import indi.dkx.laocai.model.pojo.incoming.message.IncomingMessage;
import indi.dkx.laocai.model.pojo.incoming.segment.IncomingTextSegment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventDispatcher implements ApplicationListener<ContextRefreshedEvent> {

    private final ApplicationContext applicationContext;

    // 内存里存着所有 "订阅者"
    private final List<HandlerMethod> handlers = new ArrayList<>();

    // --- 1. 扫描逻辑：Spring 启动完成后自动执行 ---
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        // 防止二次加载
        if (event.getApplicationContext().getParent() != null) return;

        Map<String, Object> beans = applicationContext.getBeansWithAnnotation(Component.class);

        beans.values().forEach(bean -> {
            Method[] methods = bean.getClass().getDeclaredMethods();
            for (Method method : methods) {
                if (method.isAnnotationPresent(Listener.class)) {
                    // 注册这个方法
                    handlers.add(new HandlerMethod(bean, method));
                    log.debug("注册事件监听器: {}.{}", bean.getClass().getSimpleName(), method.getName());
                }
            }
        });
    }

    // --- 2. 分发逻辑：由 AutoSseListener 调用 ---
    public void dispatch(Event event) {
        for (HandlerMethod handler : handlers) {
            try {
                // 1. 获取监听器方法的参数类型（例如 IncomingFriendMessage）
                Class<?> paramType = handler.method.getParameterTypes()[0];

                // 2. 准备要传给方法的参数，默认为原始事件
                Object payload = event;

                // 3. --- 智能拆包逻辑 (你提供的代码) ---
                // 如果当前事件是包装类 (IncomingMessageEvent)，但方法想要的是内部数据 (IncomingMessage及其子类)
                if (event instanceof IncomingMessageEvent<?> wrapper) {
                    if (IncomingMessage.class.isAssignableFrom(paramType)) {
                        // 提取内部 data (例如 FriendMessage)
                        payload = wrapper.getData();
                    }
                }

                // 4. 类型检查：确认 payload 真的能塞进这个方法里
                // 比如 payload 是 FriendMessage，方法参数也是 FriendMessage -> 匹配
                // 比如 payload 是 GroupMessage，方法参数是 FriendMessage -> 不匹配，跳过
                if (!paramType.isInstance(payload)) {
                    continue;
                }

                // 5. 执行 @Filter 过滤 (依然使用原始 event 或 payload 进行判断，看你 passFilter 怎么写)
                // 这里建议传 payload 进去判断，或者根据业务需求调整
                assert payload instanceof IncomingMessage;
                if (!passFilter(handler.method, (IncomingMessage) payload)) {
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
        String msgContent = extractPlainText(incomingMessage); // 获取消息纯文本

        // 简单的包含关系，你可以改成 equals 或正则
        return msgContent != null && msgContent.contains(keyword);
    }

    // --- 辅助：从复杂的 Event 结构里提取文本 (根据你的 POJO 结构) ---
    private String extractPlainText(IncomingMessage incomingMessage) {
        return incomingMessage.getSegments().stream()
                // 1. 筛选出文本类型的段
                .filter(seg -> seg instanceof IncomingTextSegment)
                // 2. 强转并提取内容
                .map(seg -> ((IncomingTextSegment) seg).getData().getText())
                // 3. 拼接成一个字符串
                .collect(Collectors.joining());
    }

    // 内部类：保存 bean 和 method 的对应关系
    record HandlerMethod(Object bean, Method method) {}
}