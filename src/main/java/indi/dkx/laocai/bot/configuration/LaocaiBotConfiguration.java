package indi.dkx.laocai.bot.configuration;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
// 启用配置属性绑定，Spring Boot 会自动处理 Record 的构造器注入
@EnableConfigurationProperties(LaocaiBotConfigurationProperties.class)
public class LaocaiBotConfiguration {

}
