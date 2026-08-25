package indi.kyson.laocai.bot

import indi.kyson.laocai.bot.response.Response
import indi.kyson.laocai.bot.response.UserProfile
import indi.kyson.laocai.bot.segment.Segment
import org.slf4j.LoggerFactory
import org.springframework.core.ParameterizedTypeReference
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.util.Optional

/**
 * 机器人领域对象。
 *
 * 承载发送/查询能力（原 BotSender 的方法），后续如果要支持多账号可以在这里扩展身份信息。
 */
class Bot(private val milkyWebClient: WebClient) {

    private val logger = LoggerFactory.getLogger(Bot::class.java)

    /**
     * 发送群消息。
     *
     * 群聊和私聊的协议字段不一样，必须按各自的 endpoint 和 body 结构分别构造。
     */
    fun sendGroupMsg(groupId: Long, segments: List<Segment>): Mono<String> {
        val body = mapOf("group_id" to groupId, "message" to segments)

        logger.debug("发送群消息: {}", body)

        return milkyWebClient.post()
            .uri("api/send_group_message")
            .bodyValue(body)
            .retrieve()
            .bodyToMono(String::class.java)
    }

    /**
     * 发送私聊消息。
     *
     * 协议把私聊对象标识单独命名为 user_id，和群消息必须分开处理。
     */
    fun sendPrivateMsg(userId: Long, segments: List<Segment>): Mono<String> {
        val body = mapOf("user_id" to userId, "message" to segments)

        return milkyWebClient.post()
            .uri("api/send_private_message")
            .bodyValue(body)
            .retrieve()
            .bodyToMono(String::class.java)
    }

    /**
     * 发送群公告。
     *
     * 公告接口需要 content 和 image_uri 这类单独字段，不能直接复用普通消息 body。
     */
    fun sendGroupAnnouncement(groupId: Long, content: String, imageUri: Optional<String>): Mono<String> {
        val body = mapOf("group_id" to groupId, "content" to content, "image_uri" to imageUri.orElse(null))

        return milkyWebClient.post()
            .uri("api/send_group_announcement")
            .bodyValue(body)
            .retrieve()
            .bodyToMono(String::class.java)
    }

    /**
     * 获取用户资料。
     *
     * 回复逻辑需要昵称等展示字段，统一走客户端封装可以避免业务代码重复拼请求。
     */
    fun getUserProfile(userId: Long): Mono<Response<UserProfile>> {
        return milkyWebClient.post()
            .uri("api/get_user_profile")
            .bodyValue(mapOf("user_id" to userId))
            .retrieve()
            .bodyToMono(object : ParameterizedTypeReference<Response<UserProfile>>() {})
    }
}
