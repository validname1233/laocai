package indi.dkx.laocai.bot.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Filter {
    /**
     * 过滤关键词，例如 "你好"
     * 只有当消息内容包含/等于该词时触发
     */
    String value() default "";

    // TODO: 比如 filterType (EQUALS, CONTAINS, REGEX)
}
