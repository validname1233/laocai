package indi.kyson.laocai.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.RedisClient;

/**
 * Redis 客户端配置。
 * <p>
 * 聊天历史和少量状态需要一个稳定的外部存储入口，集中配置比在业务代码里硬编码更可控。
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "redis")
public class RedisClientConfig {

    /**
     * Redis 主机地址。
     */
    private String host;
    /**
     * Redis 端口号。
     */
    private int port;
    /**
     * Redis 用户名。
     */
    @Value("${redis.user:default}")
    private String user;
    /**
     * Redis 密码。
     */
    private String password;

    @Bean
    public RedisClient redisClient() {
        return RedisClient.create(host, port, user, password);
    }
}


