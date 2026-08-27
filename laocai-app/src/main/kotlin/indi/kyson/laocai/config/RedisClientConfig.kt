package indi.kyson.laocai.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import redis.clients.jedis.RedisClient

@Configuration
@ConfigurationProperties(prefix = "redis")
class RedisClientConfig {

    lateinit var host: String
    var port: Int = 0
    var user: String = "default"
    lateinit var password: String

    @Bean
    fun redisClient(): RedisClient = RedisClient.create(host, port, user, password)
}
