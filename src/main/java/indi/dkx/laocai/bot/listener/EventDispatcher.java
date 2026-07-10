package indi.dkx.laocai.bot.listener;

import indi.dkx.laocai.bot.model.event.Event;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * 事件分发器。
 * <p>
 * 事件处理流程需要一个统一入口把已解析事件推送给所有监听器，而不是让每个监听器自己拉取事件。
 */
@Slf4j
@RequiredArgsConstructor
public class EventDispatcher {

    /**
     * 监听器队列。
     * <p>
     * 当前实现按注册顺序分发，先保留顺序语义，后续如果需要排序再替换容器实现。
     */
    private final List<EventListener> listenerQueue = new ArrayList<>();

    /**
     * 注册监听器。
     * <p>
     * 监听器实例由扫描阶段创建，分发器只负责保存和调用，不负责构造。
     */
    public void register(EventListener listener) {
        listenerQueue.add(listener);
    }

    /**
     * 分发事件给所有监听器。
     * <p>
     * 当前阶段还没有全局拦截器，直接顺序调用最容易保证行为可预期。
     */
    public void dispatch(Event<?> event) {
        for (EventListener listener : listenerQueue) {
            listener.handle(event);
        }
    }
}

