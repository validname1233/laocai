package indi.dkx.laocai.bot.listener;

import indi.dkx.laocai.bot.model.event.Event;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * SimpleEventDispatcherImpl 简单事件分发器
 */
@Slf4j
@RequiredArgsConstructor
public class EventDispatcher {

    /**
     * 监听器队列
     *
     * <p>用于存放监听器
     *
     * <p>TODO: 将来准备改为优先级队列
     */
    private final List<EventListener> listenerQueue = new ArrayList<>();

    /**
     * 注册方法, 用于将监听器注册到分发器中
     * @param listener 监听器
     */
    public void register(EventListener listener) {
        listenerQueue.add(listener);
    }

    /**
     * dispatchInFlow 将事件分发给所有注册的监听器
     * @param event 事件
     */
    public void dispatch(Event<?> event) {
        for (EventListener listener : listenerQueue) {
            // TODO: 暂未实现全局拦截器, 只实现了事件监听器
            listener.handle(event);
        }
    }
}