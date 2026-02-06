package indi.dkx.laocai.bot.listener;

@FunctionalInterface
public interface EventListenerResolver {
    void resolve(EventDispatcher dispatcher);
}
