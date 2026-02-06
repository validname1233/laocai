package indi.dkx.laocai.bot.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * 用于针对个事件处理器或所需处应用某些绑定器的标记注解。
 * 全局范围的绑定器无需应用，自动生效。
 *
 */
@Target(ElementType.METHOD)
public @interface ApplyBinder {

    String value() default "";
}
