package indi.dkx.laocai.bot.listener;

import indi.dkx.laocai.bot.annotation.Filter;
import indi.dkx.laocai.bot.model.event.Event;
import indi.dkx.laocai.bot.model.event.data.IncomingMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventDispatcher {

    private final List<EventListener> listenerQueue = new ArrayList<>();

    public void register(EventListener listener) {
        listenerQueue.add(listener);
    }

    // 分发逻辑：由 AutoSseListener 调用
    public void dispatch(Event<?> event) {
        for (EventListener listener : listenerQueue) {
            try {
                // TODO: 其他类型事件的过滤逻辑
                //
                //

                Object data = event.data();
                if (data instanceof IncomingMessage incomingMessage) {
                    if (!passFilter(listener.method(), incomingMessage)) {
                        continue;
                    }
                }

                listener.handle(event);
            } catch (Exception e) {
                log.error("事件分发异常", e);
            }
        }
    }

    // 辅助：过滤逻辑
    private boolean passFilter(Method method, IncomingMessage incomingMessage) {
        Filter filter = method.getAnnotation(Filter.class);
        if (filter == null || !StringUtils.hasText(filter.value())) {
            return true; // 没有注解或注解为空，直接放行
        }

        String keyword = filter.value();
        String msgContent = incomingMessage.getPlainText(); // 获取消息纯文本

        // TODO: 简单的包含关系，可以改成 equals 或正则
        return msgContent != null && msgContent.contains(keyword);
    }
}