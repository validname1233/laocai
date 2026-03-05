package indi.dkx.laocai.bot.annotation;

import indi.dkx.laocai.bot.constant.PriorityConstant;

import java.lang.annotation.*;

@Repeatable(Filters.class)
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Filter {
    /**
     * 过滤关键词，例如 "你好"
     * 只有当消息内容包含/等于该词时触发
     */
    String value() default "";

    int priority() default PriorityConstant.DEFAULT;

    Targets[] targets() default {};

    // TODO: 比如 filterType (EQUALS, CONTAINS, REGEX)

    @Retention(RetentionPolicy.SOURCE)
    @interface Targets {
        long[] users() default {};

        long[] groups() default {};

        long[] mentions() default {};

        boolean mentionBot() default false;
    }
}
