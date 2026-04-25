package indi.dkx.laocai.ai;

import lombok.Data;
import org.springframework.ai.chat.memory.repository.redis.RedisChatMemoryRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.DefaultJedisClientConfig;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisPooled;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "spring.ai.chat.memory.redis")
@Data
public class RedisChatMemoryConfig {

    @Value("${spring.data.redis.host}")
    private String host;

    @Value("${spring.data.redis.port}")
    private int port;

    @Value("${spring.data.redis.password}")
    private String password;

    private List<Map<String, String>> metadataFields;

    @Bean
    public RedisChatMemoryRepository redisChatMemoryRepository() {
        JedisPooled jedisClient = new JedisPooled(
                new HostAndPort(host, port),
                DefaultJedisClientConfig.builder()
                        .password(password)
                        .build()
        );
        return RedisChatMemoryRepository.builder()
                .jedisClient(jedisClient)
                .timeToLive(Duration.ofHours(2))
                .metadataFields(metadataFields)
                .build();
    }
}
