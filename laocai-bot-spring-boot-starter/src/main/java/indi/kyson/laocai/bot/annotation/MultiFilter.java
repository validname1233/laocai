package indi.kyson.laocai.bot.annotation;

import indi.kyson.laocai.bot.constant.PriorityConstant;

import java.lang.annotation.*;

/**
 * 组合多个 Filter 条件，支持 ANY/ALL/NONE 逻辑关系。
 * <p>
 * 重复的 @Filter 之间只能表达"全部满足"，有些场景需要"任一满足"或"全部不满足"，
 * 这种关系型组合适合单独声明，而不是硬塞进 Filter 本身。
 */
@Repeatable(MultiFilters.class)
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MultiFilter {
    /**
     * 参与组合的过滤条件。
     * <p>
     * 每个元素自身的 priority 在这里不生效，实际排序以 MultiFilter.priority 为准。
     */
    Filter[] value();

    /**
     * 组合逻辑关系。
     */
    Type type() default Type.ANY;

    /**
     * 过滤优先级。
     */
    int priority() default PriorityConstant.DEFAULT;

    enum Type {
        /** 任一条件满足即可 */
        ANY,
        /** 所有条件都要满足 */
        ALL,
        /** 所有条件都不满足才算满足 */
        NONE
    }
}
