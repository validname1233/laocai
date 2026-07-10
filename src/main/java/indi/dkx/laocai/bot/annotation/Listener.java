package indi.dkx.laocai.bot.annotation;

import indi.dkx.laocai.bot.constant.PriorityConstant;

import java.lang.annotation.*;

/**
 * 标记事件处理方法。
 * <p>
 * 监听器扫描需要一个稳定入口，才能把方法参数、过滤条件和绑定器一起解析出来。
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
public @interface Listener {
    /**
     * 事件处理器的标识。
     * <p>
     * 这个 id 主要服务日志和调试，而不是业务主流程。
     */
    String id() default "";

    /**
     * 事件处理器优先级。
     * <p>
     * 同一事件可能命中多个处理器，需要一个稳定的排序依据。
     */
    int priority() default PriorityConstant.DEFAULT;
}


