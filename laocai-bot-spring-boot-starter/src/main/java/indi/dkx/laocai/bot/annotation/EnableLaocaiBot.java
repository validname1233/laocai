package indi.dkx.laocai.bot.annotation;

import indi.dkx.laocai.bot.configuration.LaocaiBotConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 启用机器人相关配置。
 * <p>
 * 把机器人组件的装配入口集中在一个注解上，调用方只需要在启动类上显式声明即可。
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(LaocaiBotConfiguration.class)
public @interface EnableLaocaiBot {
}

