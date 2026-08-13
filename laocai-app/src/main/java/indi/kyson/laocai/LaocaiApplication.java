package indi.kyson.laocai;

import indi.kyson.laocai.bot.annotation.EnableLaocaiBot;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 应用启动入口。
 * <p>
 * 把机器人能力、Spring Boot 自动配置和定时任务统一挂在同一个启动点上，避免启动职责分散到多个配置类里。
 */
// 启用 Laocai Bot
@EnableLaocaiBot
// 启用定时任务
@EnableScheduling
@SpringBootApplication
public class LaocaiApplication {

    static void main(String[] args) {
        SpringApplication.run(LaocaiApplication.class, args);
    }

}

