package indi.dkx.laocai.bot.core;

import indi.dkx.laocai.bot.model.segment.Segment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class BotSender {

    private final WebClient milkyWebClient;

    /**
     * 发送群消息
     * @param groupId 群号
     * @param segments 消息内容（可以是纯文本，也可以是 CQ 码）
     */
    public void sendGroupMsg(Long groupId, List<Segment> segments) {
        // 构建请求体 (Milky 标准)
        Map<String, Object> body = new HashMap<>();
        body.put("group_id", groupId);
        body.put("message", segments);

        log.debug("发送群消息: {}", body);

        // 发送 POST 请求
        milkyWebClient.post()
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
     * @param userId QQ号
     * @param segments 消息内容（可以是纯文本，也可以是 CQ 码）
     */
    public void sendPrivateMsg(Long userId, List<Segment> segments) {
        Map<String, Object> body = new HashMap<>();
        body.put("user_id", userId);
        body.put("message", segments);

        milkyWebClient.post()
                .uri("/api/send_private_message")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class)
                .subscribe(
                        resp -> log.debug("私聊发送成功: {}", resp),
                        err -> log.error("私聊发送失败", err)
                );
    }


    public void sendGroupAnnouncement(Long groupId, String content, Optional<String> imageUri) {
        Map<String, Object> body = new HashMap<>();
        body.put("group_id", groupId);
        body.put("content", content);
        body.put("image_uri", imageUri.orElse(null));

        milkyWebClient.post()
                .uri("/api/send_group_announcement")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class)
                .subscribe(
                        resp -> {
                            log.info("群公告发送成功: {}", resp);
                        },
                        err -> log.error("群公告发送失败", err)
                );
    }
}