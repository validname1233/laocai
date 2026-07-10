package indi.dkx.laocai.bot.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * 为单个事件处理方法指定绑定器。
 * <p>
 * 大多数绑定规则可以全局生效，但少数处理器需要额外绑定时，应该允许在方法级单独声明。
 */
@Target(ElementType.METHOD)
public @interface ApplyBinder {

    String value() default "";
}

