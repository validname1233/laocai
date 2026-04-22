package indi.dkx.laocai.ai;

import lombok.Data;
import org.springframework.ai.chat.memory.repository.redis.RedisChatMemoryRepository;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.DefaultJedisClientConfig;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisPooled;

import java.time.Duration;

@Configuration
@ConfigurationProperties(prefix = "spring.data.redis")
@Data
public class RedisChatMemoryConfig {

    private String host;

    private int port;

    private String user;

    private String password;

    private int database;

    @Bean
    public RedisChatMemoryRepository redisChatMemoryRepository() {
        JedisPooled jedisClient = new JedisPooled(
                new HostAndPort(host, port),
                DefaultJedisClientConfig.builder()
                        .user(user)
                        .password(password)
                        .build()
        );
        return RedisChatMemoryRepository.builder()
                .jedisClient(jedisClient)
                .timeToLive(Duration.ofHours(2))
                .build();
    }
}
