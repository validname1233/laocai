package indi.kyson.laocai.bot.configuration

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.DefaultValue

@ConfigurationProperties(prefix = "laocai")
data class LaocaiBotConfigurationProperties(
    @DefaultValue val milky: Milky,
    @DefaultValue val dispatcher: Dispatcher,
) {

    /**
     * Milky（LLBot 网关）远端连接配置。
     *
     * url 用于触发自动配置；access token 为空时不发送 Authorization 请求头。
     */
    data class Milky(
        val url: String,
        val accessToken: String? = null,
        val enabled: Boolean = true,
    )

    /**
     * 事件分发配置。
     *
     * 并发度和缓冲上限会一起影响 SSE 消费的稳定性，放在同一个配置块里更容易理解。
     * @param concurrency 事件分发并发度
     * @param bufferSize 事件积压缓冲上限
     */
    data class Dispatcher(
        @DefaultValue("32") val concurrency: Int,
        @DefaultValue("5000") val bufferSize: Int,
    )
}
