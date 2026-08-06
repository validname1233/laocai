package indi.dkx.laocai.bot.core;

import indi.dkx.laocai.bot.model.response.Response;
import indi.dkx.laocai.bot.model.response.data.UserProfile;
import indi.dkx.laocai.bot.model.segment.Segment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class BotSender {

    private final WebClient milkyWebClient;

    /**
     * 发送群消息。
     * <p>
     * 群聊和私聊的协议字段不一样，必须按各自的 endpoint 和 body 结构分别构造。
     * @param groupId 群号
     * @param segments 消息内容（可以是纯文本，也可以是 CQ 码）
     */
    public Mono<String> sendGroupMsg(Long groupId, List<Segment> segments) {
        Map<String, Object> body = new HashMap<>();
        body.put("group_id", groupId);
        body.put("message", segments);

        log.debug("发送群消息: {}", body);

        return milkyWebClient.post()
                .uri("/api/send_group_message")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class);
    }

    /**
     * 发送私聊消息。
     * <p>
     * 协议把私聊对象标识单独命名为 user_id，和群消息必须分开处理。
     * @param userId QQ号
     * @param segments 消息内容（可以是纯文本，也可以是 CQ 码）
     */
    public Mono<String> sendPrivateMsg(Long userId, List<Segment> segments) {
        Map<String, Object> body = new HashMap<>();
        body.put("user_id", userId);
        body.put("message", segments);

        return milkyWebClient.post()
                .uri("/api/send_private_message")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class);
    }

    /**
     * 发送群公告。
     * <p>
     * 公告接口需要 content 和 image_uri 这类单独字段，不能直接复用普通消息 body。
     */
    public Mono<String> sendGroupAnnouncement(Long groupId, String content, Optional<String> imageUri) {
        Map<String, Object> body = new HashMap<>();
        body.put("group_id", groupId);
        body.put("content", content);
        body.put("image_uri", imageUri.orElse(null));

        return milkyWebClient.post()
                .uri("/api/send_group_announcement")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class);
    }

    /**
     * 获取用户资料。
     * <p>
     * 回复逻辑需要昵称等展示字段，统一走客户端封装可以避免业务代码重复拼请求。
     */
    public Mono<Response<UserProfile>> getUserProfile(Long userId) {
        return milkyWebClient.post()
                .uri("/api/get_user_profile")
                .bodyValue(Map.of("user_id", userId))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<>() {});
    }
}

