package indi.dkx.laocai.bot.annotation;

import indi.dkx.laocai.bot.constant.PriorityConstant;

import java.lang.annotation.*;

/**
 * 事件过滤注解。
 * <p>
 * 把消息筛选条件贴近处理器声明，能让监听器的触发条件和处理逻辑一起阅读。
 */
@Repeatable(Filters.class)
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Filter {
    /**
     * 过滤关键词。
     * <p>
     * 大部分监听器都要先限制消息内容，再决定是否进入后续处理。
     */
    String value() default "";

    /**
     * 过滤优先级。
     * <p>
     * 多个过滤条件需要按约定顺序组合，避免低优先级规则先短路。
     */
    int priority() default PriorityConstant.DEFAULT;

    /**
     * 除文本外的其他匹配条件。
     * <p>
     * 有些监听器要基于发送者、群号或 @ 关系判断，而不只是内容匹配。
     */
    Targets[] targets() default {};

    // TODO: 比如 filterType (EQUALS, CONTAINS, REGEX)

    @Retention(RetentionPolicy.SOURCE)
    @interface Targets {

        /**
         * 按消息发送者 QQ 号匹配。
         */
        long[] users() default {};

        /**
         * 按消息发送所在群号匹配。
         */
        long[] groups() default {};

        /**
         * 按被 @ 的 QQ 号匹配。
         */
        long[] mentions() default {};

        /**
         * 是否 @ 机器人自己。
         */
        boolean mentionBot() default false;
    }
}


