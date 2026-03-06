package indi.dkx.laocai.bot.annotation;

import indi.dkx.laocai.bot.constant.PriorityConstant;

import java.lang.annotation.*;

/**
 * 消息过滤器注解, 用于简化开发者对消息的过滤逻辑
 */
@Repeatable(Filters.class)
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Filter {
    /**
     * 过滤关键词，例如 "你好"
     * 只有当消息内容包含/等于该词时触发
     */
    String value() default "";

    /**
     * 优先级
     */
    int priority() default PriorityConstant.DEFAULT;

    /**
     * 除了文本匹配的其他匹配方法
     */
    Targets[] targets() default {};

    // TODO: 比如 filterType (EQUALS, CONTAINS, REGEX)

    @Retention(RetentionPolicy.SOURCE)
    @interface Targets {

        /**
         * 按消息发送者 QQ号匹配
         */
        long[] users() default {};

        /**
         * 按消息发送所在群号匹配
         */
        long[] groups() default {};

        /**
         * 按 @ 的 QQ 号匹配
         */
        long[] mentions() default {};

        /**
         * 是否 @ 机器人自己
         */
        boolean mentionBot() default false;
    }
}
