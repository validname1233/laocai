package indi.dkx.laocai.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "qq-bot")
public class QQBotConfig {
    private String appid;
    private String secret;
    private String baseUrl;
}
