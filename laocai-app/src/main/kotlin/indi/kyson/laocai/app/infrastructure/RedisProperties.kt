package indi.kyson.laocai.app.infrastructure

import org.springframework.boot.context.properties.ConfigurationProperties

/** Redis 连接配置，绑定 `redis.*`。 */
@ConfigurationProperties(prefix = "redis")
data class RedisProperties(
    val host: String,
    val port: Int,
    val password: String,
    val user: String = "default",
)
