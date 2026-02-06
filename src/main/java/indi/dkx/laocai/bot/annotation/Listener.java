package indi.dkx.laocai.bot.annotation;

import indi.dkx.laocai.bot.constant.PriorityConstant;

import java.lang.annotation.*;

/**
 * 标记一个函数为监听函数/事件处理器。
 * 被 [Listener] 标记的函数在进行处理的时候会根据此函数的参数尝试自动分析其监听目标。
 * 一个事件处理器建议只有**一个** [Event] 类型的参数。
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
public @interface Listener {
    /**
     * 此事件处理器的id。通常用于日志输出或调试用。默认会根据函数生成一个ID
     * @return 事件处理器的id
     */
    String id() default "";

    /**
     * 此事件处理器的优先级
     * @return 事件处理器的优先级
     */
    int priority() default PriorityConstant.DEFAULT;
}
