package indi.dkx.laocai.bot.listener;

import indi.dkx.laocai.bot.annotation.Filter;
import indi.dkx.laocai.bot.annotation.Listener;
import indi.dkx.laocai.bot.model.event.Event;
import indi.dkx.laocai.bot.model.event.data.IncomingMessage;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

/**
 * 把带注解的方法转换成可注册的事件监听器解析器。
 * <p>
 * 扫描阶段只负责收集元数据，真正的实例化和匹配器拼装要延迟到容器可用之后。
 */
public class EventListenerProcessor {

    /**
     * 把一个监听方法转换成延迟注册器。
     * <p>
     * 方法上的参数类型、Filter 和 Binder 需要先统一成 matcher，再交给分发器持有。
     * @param beanName 监听器名称
     * @param method 监听器方法
     * @param listenerAnnotation 监听器注解
     * @param applyBinder 绑定器注解
     * @param applicationContext spring上下文
     * @param binderManager 绑定器管理器
     * @return EventListenerResolver
     */
    public EventListenerResolver process(
            String beanName,
            Method method,
            Listener listenerAnnotation,
            ApplicationContext applicationContext
    ) {
        String id = listenerAnnotation.id();
        int priority = listenerAnnotation.priority();
        Type[] listenTarget = method.getGenericParameterTypes();

        List<Predicate<Event<?>>> matchers = new ArrayList<>();
        // 先校验参数类型，再叠加注解过滤条件，避免对明显不匹配的事件多做判断。
        matchers.add((Event<?> event) -> matchParam(method, event));

        List<FilterData> filterDataList = getFilterDataList(method);
        // Filter 是可重复的，所以这里要按优先级排序后统一合并。
        matchers.addAll(filterDataList.stream()
                .sorted(Comparator.comparingInt(FilterData::priority).reversed())
                .map(FilterData::matcher).toList());

        return (EventDispatcher dispatcher) -> {
            Object instance = applicationContext.getBean(beanName);
            if (!method.canAccess(instance)) method.setAccessible(true);
            var listener = new EventListener(
                    instance,
                    method,
                    (Event<?> event) -> matchers
                            .stream()
                            .allMatch((Predicate<Event<?>> matcher) -> matcher.test(event))
            );
            dispatcher.register(listener);
        };
    }

    /**
     * 判断方法参数类型是否与事件数据匹配
     *
     * @param event 事件数据
     * @return 是否匹配
     */
    private boolean matchParam(Method method, Event<?> event) {
        // 获取方法第一个参数的泛型类型
        Type genericParam = method.getGenericParameterTypes()[0];
        // 检查是否是带泛型的 Event<T> 类型
        if (genericParam instanceof ParameterizedType parameterizedType
                && parameterizedType.getRawType() == Event.class) {
            // 获取泛型的实际类型参数 T
            Type actualType = parameterizedType.getActualTypeArguments()[0];
            // 如果 T 是具体的 Class，检查 data 是否是该类型的实例
            if (actualType instanceof Class<?> actualClass) {
                return actualClass.isInstance(event.data());
            }
        }
        // 如果参数是原始 Event 类型（无泛型），只要 data 不为空就匹配
        Class<?> paramType = method.getParameterTypes()[0];
        return paramType == Event.class && event.data() != null;
    }

    /**
     * 将方法上的所有 Filter 注解数据转换为 FilterData 列表
     * @param method 方法
     * @return FilterData 列表
     */
    private List<FilterData> getFilterDataList(Method method) {
        return MergedAnnotations.from(method).stream(Filter.class).map(mergedAnnotation -> {
            // 获取 Filter 注解中的 String value 匹配器
            Predicate<Event<?>> keywordMatcher = getKeywordMatcher(mergedAnnotation);
            // 获取 Filter 注解中的 Targets注解 匹配器
            Predicate<Event<?>> targetMatcher = getTargetMatcher(mergedAnnotation);

            // 组合两个匹配器，要求同时满足
            Predicate<Event<?>> combinedMatcher
                    = (Event<?> event) -> keywordMatcher.test(event) && targetMatcher.test(event);
            // 返回 FilterData 对象
            return new FilterData(mergedAnnotation.getInt("priority"), combinedMatcher);
        }).toList();
    }

    /**
     * 获取 Filter 注解中的 String value 匹配器
     * @param mergedAnnotation Filter 注解数据
     * @return String value 匹配器
     */
    private Predicate<Event<?>> getKeywordMatcher(MergedAnnotation<Filter> mergedAnnotation) {
        // 获取 Filter 注解中的 String value, 这里解析为正则表达式
        String value = mergedAnnotation.getString("value");
        // 如果 value 为空，则返回 true 匹配器
        if (!StringUtils.hasText(value)) return (Event<?> _) -> true;
        else return (Event<?> event) -> {
            Object data = event.data();
            if (data instanceof IncomingMessage incomingMessage) {
                String msgContent = incomingMessage.getPlainText();
                // 如果 msgContent 不为空且匹配 value，则返回 true
                return msgContent != null && msgContent.matches(value);
            }
            return false;
        };
    }

    /**
     * 获取 Filter 注解中的 Targets 注解匹配器
     * @param mergedAnnotation Filter 注解数据
     * @return Targets 注解匹配器
     */
    private Predicate<Event<?>> getTargetMatcher(MergedAnnotation<Filter> mergedAnnotation) {
        // TODO: 还没实现, 默认返回 true 匹配器
        return (Event<?> _) -> true;
    }
}


