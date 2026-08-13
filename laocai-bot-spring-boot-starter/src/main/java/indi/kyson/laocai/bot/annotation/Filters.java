package indi.kyson.laocai.bot.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 让一个方法可以声明多个过滤条件。
 * <p>
 * 复杂监听器常常需要多个独立规则，重复写方法比合并成一个大条件更清晰。
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Filters {
    Filter[] value();
}


