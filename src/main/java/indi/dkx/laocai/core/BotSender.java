package indi.dkx.laocai.core;

import indi.dkx.laocai.model.pojo.incoming.segment.IncomingSegment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class BotSender {

    private final WebClient webClient;

    /**
     * 手动构造函数注入
     * Spring 会自动把 Builder 和 botUrl 传进来
     */
    public BotSender(WebClient.Builder webClientBuilder,
                     @Value("${laocai.bot.url:http://localhost:3010}") String botUrl) {
        // 创建单例 WebClient，并设置好 BaseUrl
        this.webClient = webClientBuilder.baseUrl(botUrl).build();
    }

    /**
     * 发送群消息
     * @param groupId 群号
     * @param segments 消息内容（可以是纯文本，也可以是 CQ 码）
     */
    public void sendGroupMsg(Long groupId, List<IncomingSegment> segments) {
        // 构建请求体 (Milky 标准)
        Map<String, Object> body = new HashMap<>();
        body.put("group_id", groupId);
        body.put("message", segments);

        log.debug("发送群消息: {}", body);

        // 发送 POST 请求
        webClient.post()
                .uri("/api/send_group_message") // OneBot 发送群消息的端点
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class) // 获取响应结果
                .subscribe(
                        response -> log.debug("消息发送成功: {}", response),
                        error -> log.error("消息发送失败", error)
                );
    }

    /**
     * 发送私聊消息
     */
    public void sendPrivateMsg(Long userId, List<IncomingSegment> segments) {
        Map<String, Object> body = new HashMap<>();
        body.put("user_id", userId);
        body.put("message", segments);

        webClient.post()
                .uri("/api/send_private_message")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class)
                .subscribe(
                        resp -> log.debug("私聊发送成功: {}", resp),
                        err -> log.error("私聊发送失败", err)
                );
    }
}