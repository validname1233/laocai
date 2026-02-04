package indi.dkx.laocai.bot.annotation;

import indi.dkx.laocai.bot.config.LaocaiBotConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target(ElementType.TYPE) // 只能用在类上
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(LaocaiBotConfiguration.class) // <--- 核心：导入上面的配置类
public @interface EnableLaocaiBot {
}
