package indi.kyson.laocai.bot.listener;

/**
 * 事件监听器解析器。
 * <p>
 * 扫描阶段只需要一个延迟注册入口，真正实例化和绑定动作放到容器就绪后再执行。
 */
@FunctionalInterface
public interface EventListenerResolver {
    void resolve(EventDispatcher dispatcher);
}


