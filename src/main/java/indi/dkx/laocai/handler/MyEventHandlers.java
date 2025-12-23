package indi.dkx.laocai.handler;

import lombok.extern.slf4j.Slf4j;
import love.forte.simbot.component.onebot.v11.core.event.message.OneBotFriendMessageEvent;
import love.forte.simbot.event.ChatGroupMessageEvent;
import love.forte.simbot.message.At;
import love.forte.simbot.message.Message;
import love.forte.simbot.quantcat.common.annotations.ContentTrim;
import love.forte.simbot.quantcat.common.annotations.Filter;
import love.forte.simbot.quantcat.common.annotations.Listener;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
public class MyEventHandlers {
    /**
     * 此处是一个标准库中通用的类型：聊天群消息事件
     */
    @Listener
    @ContentTrim
    @Filter("摸摸")
    public void onGroupMessage(ChatGroupMessageEvent event) {
        log.debug("ChatGroupMessageEvent: {}", event);
        event.getMessageContent().getMessages().forEach(message -> {
            if (message instanceof At) event.replyAsync("喵! ");
        });

    }

    /**
     * 此处监听的是OneBot组件中的专属类型：OneBot的好友消息事件
     * 并且过滤消息：消息中的文本消息去除前后空字符后，等于 '你好'
     */
    @Listener
    @ContentTrim
    @Filter("你好")
    public CompletableFuture<?> onFriendMessage(OneBotFriendMessageEvent event) {
        for (Message.Element message : event.getMessageContent().getMessages()) {
            log.info("Message: {}", message);
        }
        return event.replyAsync("你也好");
        // 可以直接返回任意 CompletableFuture 类型
    }
}
