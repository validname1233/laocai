package indi.kyson.laocai.bot.listener;

import indi.kyson.laocai.bot.annotation.Filter;
import indi.kyson.laocai.bot.annotation.Listener;
import indi.kyson.laocai.bot.annotation.MatchType;
import indi.kyson.laocai.bot.annotation.MultiFilter;
import indi.kyson.laocai.bot.model.event.Event;
import indi.kyson.laocai.bot.model.event.data.IncomingGroupMessage;
import indi.kyson.laocai.bot.model.event.data.IncomingMessage;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * 把带注解的方法转换成可注册的事件监听器解析器。
 * <p>
 * 扫描阶段只负责收集元数据，真正的实例化和匹配器拼装要延迟到容器可用之后。
 */
public class EventListenerProcessor {

    /**
     * 把一个监听方法转换成。
     * <p>
     * 方法上的参数类型、Filter注解、MuiltiFilter注解需要先统一成 matcher，再交给分发器持有。
     * @param beanName 被Component注解的bean实例的名称
     * @param method bean实例中被Listener注解的一个方法
     * @param listenerAnnotation Listener注解
     * @param applicationContext spring上下文
     * @return EventListenerResolver
     */
    public EventListenerResolver process(
            String beanName,
            Method method,
            Listener listenerAnnotation,
            ApplicationContext applicationContext
    ) {
        List<Predicate<Event<?>>> matchers = new ArrayList<>();
        // 先校验参数类型，再叠加注解过滤条件，避免对明显不匹配的事件多做判断。
        matchers.add((Event<?> event) -> matchParam(method, event));

        List<FilterData> filterDataList = getFilterDataList(method);
        // Filter 是可重复的，所以这里要按优先级排序后统一合并。
        matchers.addAll(filterDataList.stream()
                .sorted(Comparator.comparingInt(FilterData::priority).reversed())
                .map(FilterData::matcher).toList());

        return (EventDispatcher dispatcher) -> {
            // 从 Spring 上下文中获取 beanName 对应的实例
            Object instance = applicationContext.getBean(beanName);
            // 反射爆破
            if (!method.canAccess(instance)) method.setAccessible(true);
            // 创建 EventListener 实例
            var listener = new EventListener(
                    instance,
                    method,
                    (Event<?> event) -> matchers
                            .stream()
                            .allMatch((Predicate<Event<?>> matcher) -> matcher.test(event))
            );
            // 将 Eventlistener 注册到 dispatcher
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
     * 将方法上的所有 Filter / MultiFilter 注解数据转换为 FilterData 列表。
     * <p>
     * 单独重复的 @Filter 之间是"全部满足"（AND）；@MultiFilter 内部按其 type 声明的
     * ANY/ALL/NONE 逻辑组合一组 @Filter，作为整体再参与外层的 AND 组合。
     * @param method 方法
     * @return FilterData 列表
     */
    private List<FilterData> getFilterDataList(Method method) {
        // 结果List, 用于存储最终结果
        List<FilterData> result = new ArrayList<>();

        MergedAnnotations.from(method)
        .stream(Filter.class)
        .forEach(mergedAnnotation -> result.add(
            new FilterData(mergedAnnotation.getInt("priority"), toFilterMatcher(mergedAnnotation))
        ));

        MergedAnnotations.from(method)
        .stream(MultiFilter.class)
        .forEach(mergedAnnotation -> result.add(
            new FilterData(mergedAnnotation.getInt("priority"), toMultiFilterMatcher(mergedAnnotation))
        ));

        return result;
    }

    /**
     * 将单个 Filter 注解转换为匹配器（关键词匹配 + Targets 匹配同时满足）
     * @param mergedAnnotation Filter 注解数据
     * @return 匹配器
     */
    private Predicate<Event<?>> toFilterMatcher(MergedAnnotation<Filter> mergedAnnotation) {
        Predicate<Event<?>> keywordMatcher = getKeywordMatcher(mergedAnnotation);
        Predicate<Event<?>> targetMatcher = getTargetMatcher(mergedAnnotation);
        return (Event<?> event) -> keywordMatcher.test(event) && targetMatcher.test(event);
    }

    /**
     * 将 MultiFilter 注解转换为匹配器，按其 type 组合内部的一组 Filter
     * @param mergedAnnotation MultiFilter 注解数据
     * @return 匹配器
     */
    private Predicate<Event<?>> toMultiFilterMatcher(MergedAnnotation<MultiFilter> mergedAnnotation) {
        // 获取 MultiFilter注解 的 value 属性中所有的 Filter 注解
        MergedAnnotation<Filter>[] filters = mergedAnnotation.getAnnotationArray("value", Filter.class);
        List<Predicate<Event<?>>> subMatchers = Arrays.stream(filters).map(this::toFilterMatcher).toList();
        MultiFilter.Type type = mergedAnnotation.getEnum("type", MultiFilter.Type.class);

        return switch (type) {
            case ANY -> (Event<?> event) -> subMatchers.stream().anyMatch(matcher -> matcher.test(event));
            case ALL -> (Event<?> event) -> subMatchers.stream().allMatch(matcher -> matcher.test(event));
            case NONE -> (Event<?> event) -> subMatchers.stream().noneMatch(matcher -> matcher.test(event));
        };
    }

    /**
     * 获取 Filter 注解中的 String value 匹配器
     * @param mergedAnnotation Filter 注解数据
     * @return String value 匹配器
     */
    private Predicate<Event<?>> getKeywordMatcher(MergedAnnotation<Filter> mergedAnnotation) {
        String value = mergedAnnotation.getString("value");
        // 如果 value 为空，则返回 true 匹配器
        if (!StringUtils.hasText(value)) return (Event<?> _) -> true;

        MatchType matchType = mergedAnnotation.getEnum("matchType", MatchType.class);
        return (Event<?> event) -> {
            Object data = event.data();
            if (!(data instanceof IncomingMessage incomingMessage)) return false;
            String msgContent = incomingMessage.getPlainText();
            if (msgContent == null) return false;

            return switch (matchType) {
                case EQUALS -> msgContent.equals(value);
                case EQUALS_IGNORE_CASE -> msgContent.equalsIgnoreCase(value);
                case STARTS_WITH -> msgContent.startsWith(value);
                case ENDS_WITH -> msgContent.endsWith(value);
                case CONTAINS -> msgContent.contains(value);
                case REGEX -> msgContent.matches(value);
                case REGEX_CONTAINS -> Pattern.compile(value).matcher(msgContent).find();
            };
        };
    }

    /**
     * 获取 Filter 注解中的 Targets 注解匹配器
     * <p>
     * targets 是数组，多个 Targets 之间是"任一满足即可"（OR）；
     * 单个 Targets 内部的 users/groups/mentions/mentionBot 各维度之间是"全部满足"（AND），
     * 某个维度未声明（空数组/false）时视为不限制该维度。
     * @param mergedAnnotation Filter 注解数据
     * @return Targets 注解匹配器
     */
    private Predicate<Event<?>> getTargetMatcher(MergedAnnotation<Filter> mergedAnnotation) {
        MergedAnnotation<Filter.Targets>[] targetsAnnotations =
                mergedAnnotation.getAnnotationArray("targets", Filter.Targets.class);
        // 没有声明任何 Targets，则不限制
        if (targetsAnnotations.length == 0) return (Event<?> _) -> true;

        List<Predicate<Event<?>>> targetMatchers = Arrays.stream(targetsAnnotations)
                .map(this::toTargetMatcher)
                .toList();
        return (Event<?> event) -> targetMatchers.stream().anyMatch(matcher -> matcher.test(event));
    }

    /**
     * 将单个 Targets 注解转换为匹配器
     * @param targets Targets 注解数据
     * @return 匹配器
     */
    private Predicate<Event<?>> toTargetMatcher(MergedAnnotation<Filter.Targets> targets) {
        long[] users = targets.getLongArray("users");
        long[] groups = targets.getLongArray("groups");
        long[] mentions = targets.getLongArray("mentions");
        boolean mentionBot = targets.getBoolean("mentionBot");

        return (Event<?> event) -> {
            if (!(event.data() instanceof IncomingMessage message)) return false;

            if (users.length > 0
                    && (message.getSenderId() == null || !contains(users, message.getSenderId()))) {
                return false;
            }
            if (groups.length > 0) {
                if (!(message instanceof IncomingGroupMessage groupMessage)) return false;
                if (groupMessage.getPeerId() == null || !contains(groups, groupMessage.getPeerId())) return false;
            }
            if (mentions.length > 0) {
                long[] mentioned = message.getMentionedUserIds();
                if (Arrays.stream(mentions).noneMatch(id -> contains(mentioned, id))) return false;
            }
            if (mentionBot) {
                if (event.selfId() == null || !contains(message.getMentionedUserIds(), event.selfId())) return false;
            }
            return true;
        };
    }

    /**
     * 判断 long 数组中是否包含目标值
     */
    private static boolean contains(long[] array, long value) {
        for (long element : array) {
            if (element == value) return true;
        }
        return false;
    }
}


