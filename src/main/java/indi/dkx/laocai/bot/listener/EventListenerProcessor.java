package indi.dkx.laocai.bot.listener;

import indi.dkx.laocai.bot.annotation.ApplyBinder;
import indi.dkx.laocai.bot.annotation.Filter;
import indi.dkx.laocai.bot.annotation.Listener;
import indi.dkx.laocai.bot.binder.BinderManager;
import indi.dkx.laocai.bot.model.event.Event;
import indi.dkx.laocai.bot.model.event.data.IncomingMessage;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

@Component
public class EventListenerProcessor {

    /**
     * 处理监听器方法
     *
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
            ApplyBinder applyBinder,
            ApplicationContext applicationContext,
            BinderManager binderManager
    ) {
        // 读取Listener注解中的id
        String id = listenerAnnotation.id();
        // 读取Listener注解中的priority
        int priority = listenerAnnotation.priority();
        // 获取这个函数监听的第一个参数的Event
        Type[] listenTarget = method.getGenericParameterTypes();

        // 用于存储匹配器
        List<Predicate<Event<?>>> matchers = new ArrayList<>();
        // 判断方法参数类型是否与事件数据匹配
        matchers.add((Event<?> event) -> matchParam(method, event));

        List<FilterData> filterDataList = getFilterDataList(method);
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
            // TODO
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

    // TODO
    private List<FilterData> getFilterDataList(Method method) {
        return MergedAnnotations.from(method).stream(Filter.class).map(mergedAnnotation -> {
            Predicate<Event<?>> keywordMatcher = getKeywordMatcher(mergedAnnotation);
            Predicate<Event<?>> targetMatcher = getTargetMatcher(mergedAnnotation);

            Predicate<Event<?>> combinedMatcher
                    = (Event<?> event) -> keywordMatcher.test(event) && targetMatcher.test(event);

            return new FilterData(mergedAnnotation.getInt("priority"), combinedMatcher);
        }).toList();
    }

    // TODO
    private Predicate<Event<?>> getKeywordMatcher(MergedAnnotation<Filter> mergedAnnotation) {
        String value = mergedAnnotation.getString("value");

        if (!StringUtils.hasText(value)) return (Event<?> _) -> true;
        else return (Event<?> event) -> {
            Object data = event.data();
            if (data instanceof IncomingMessage incomingMessage) {
                String msgContent = incomingMessage.getPlainText();
                return msgContent != null && msgContent.matches(value);
            }
            return false;
        };
    }

    // TODO
    private Predicate<Event<?>> getTargetMatcher(MergedAnnotation<Filter> mergedAnnotation) {
        return (Event<?> _) -> true;
    }
}
