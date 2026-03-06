package indi.dkx.laocai.bot.listener;

/**
 * 事件监听器解析器
 */
@FunctionalInterface
public interface EventListenerResolver {
    void resolve(EventDispatcher dispatcher);
}
