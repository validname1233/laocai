package indi.dkx.laocai.bot.listener;

import indi.dkx.laocai.bot.model.event.Event;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * SimpleEventDispatcherImpl 简单事件分发器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EventDispatcher {

    private final List<EventListener> listenerQueue = new ArrayList<>();

    public void register(EventListener listener) {
        listenerQueue.add(listener);
    }

    /**
     * dispatchInFlow 将事件分发给所有注册的监听器
     * @param event 事件
     */
    public void dispatch(Event<?> event) {
        for (EventListener listener : listenerQueue) {
            // TODO
            listener.handle(event);
        }
    }
}