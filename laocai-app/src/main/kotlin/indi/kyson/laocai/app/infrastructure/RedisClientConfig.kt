package indi.kyson.laocai.app.infrastructure

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import redis.clients.jedis.RedisClient

@Configuration
@EnableConfigurationProperties(RedisProperties::class)
class RedisClientConfig {

    @Bean
    fun redisClient(properties: RedisProperties): RedisClient =
        RedisClient.create(properties.host, properties.port, properties.user, properties.password)
}
