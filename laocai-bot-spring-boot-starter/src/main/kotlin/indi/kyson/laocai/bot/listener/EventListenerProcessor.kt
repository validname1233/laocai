package indi.kyson.laocai.bot.listener

import indi.kyson.laocai.bot.annotation.Filter
import indi.kyson.laocai.bot.annotation.Listener
import indi.kyson.laocai.bot.annotation.MatchType
import indi.kyson.laocai.bot.annotation.MultiFilter
import indi.kyson.laocai.bot.constant.PriorityConstant
import indi.kyson.laocai.bot.event.Event
import indi.kyson.laocai.bot.event.GroupMessageEvent
import indi.kyson.laocai.bot.event.MessageEvent
import org.springframework.core.annotation.AnnotatedElementUtils
import org.springframework.core.annotation.MergedAnnotation
import org.springframework.core.annotation.MergedAnnotations
import org.springframework.util.StringUtils
import java.lang.reflect.Method
import java.util.regex.Pattern

/**
 * 把带注解的方法转换成可注册的事件监听器解析器。
 *
 * 扫描阶段只负责收集元数据，真正的实例化和匹配器拼装要延迟到容器可用之后。
 */
internal class EventListenerProcessor {

    /**
     * 把一个监听方法转换成 [EventListenerResolver]。
     *
     * 方法上的参数类型、Filter 注解、MultiFilter 注解需要先统一成 matcher，再交给分发器持有。
     *
     * @param beanName 被扫描到的 bean 名称
     * @param method 被 [indi.kyson.laocai.bot.annotation.Listener] 注解的方法
     * @param beanProvider 按 beanName 获取实例的函数，由容器集成层（如 Spring）提供，使匹配逻辑不依赖具体容器
     */
    fun process(
        beanName: String,
        method: Method,
        beanProvider: (String) -> Any,
    ): EventListenerResolver {
        val matchers = mutableListOf<(Event) -> Boolean>()
        // 先校验参数类型，再叠加注解过滤条件，避免对明显不匹配的事件多做判断。
        matchers.add { event -> matchParam(method, event) }

        val filterDataList = getFilterDataList(method)
        // Filter 是可重复的，所以这里要按优先级排序后统一合并。
        matchers.addAll(filterDataList.sortedByDescending { it.priority }.map { it.matcher })

        val priority = AnnotatedElementUtils.findMergedAnnotation(method, Listener::class.java)?.priority
            ?: PriorityConstant.DEFAULT

        return EventListenerResolver { dispatcher ->
            val instance = beanProvider(beanName)
            if (!method.canAccess(instance)) method.isAccessible = true
            val listener = EventListener(instance, method, priority) { event ->
                matchers.all { matcher -> matcher(event) }
            }
            dispatcher.register(listener)
        }
    }

    /**
     * 判断方法参数的声明类型是否与事件实例匹配。
     *
     * Event 不再是泛型信封，直接按参数声明类型做多态判断即可，
     * 无需再反射掏 ParameterizedType 的实际类型参数。
     */
    private fun matchParam(method: Method, event: Event): Boolean {
        val paramType = method.parameterTypes[0]
        return paramType.isInstance(event)
    }

    /**
     * 将方法上的所有 Filter / MultiFilter 注解数据转换为 FilterData 列表。
     *
     * 单独重复的 @Filter 之间是"全部满足"（AND）；@MultiFilter 内部按其 type 声明的
     * ANY/ALL/NONE 逻辑组合一组 @Filter，作为整体再参与外层的 AND 组合。
     */
    private fun getFilterDataList(method: Method): List<FilterData> {
        val result = mutableListOf<FilterData>()

        MergedAnnotations.from(method).stream(Filter::class.java).forEach { mergedAnnotation ->
            result.add(FilterData(mergedAnnotation.getInt("priority"), toFilterMatcher(mergedAnnotation)))
        }

        MergedAnnotations.from(method).stream(MultiFilter::class.java).forEach { mergedAnnotation ->
            result.add(FilterData(mergedAnnotation.getInt("priority"), toMultiFilterMatcher(mergedAnnotation)))
        }

        return result
    }

    /**
     * 将单个 Filter 注解转换为匹配器（关键词匹配 + Targets 匹配同时满足）
     */
    private fun toFilterMatcher(mergedAnnotation: MergedAnnotation<Filter>): (Event) -> Boolean {
        val keywordMatcher = getKeywordMatcher(mergedAnnotation)
        val targetMatcher = getTargetMatcher(mergedAnnotation)
        return { event -> keywordMatcher(event) && targetMatcher(event) }
    }

    /**
     * 将 MultiFilter 注解转换为匹配器，按其 type 组合内部的一组 Filter
     */
    private fun toMultiFilterMatcher(mergedAnnotation: MergedAnnotation<MultiFilter>): (Event) -> Boolean {
        val filters = mergedAnnotation.getAnnotationArray("value", Filter::class.java)
        val subMatchers = filters.map { toFilterMatcher(it) }
        val type = mergedAnnotation.getEnum("type", MultiFilter.Type::class.java)

        return when (type) {
            MultiFilter.Type.ANY -> { event -> subMatchers.any { it(event) } }
            MultiFilter.Type.ALL -> { event -> subMatchers.all { it(event) } }
            MultiFilter.Type.NONE -> { event -> subMatchers.none { it(event) } }
        }
    }

    /**
     * 获取 Filter 注解中的 String value 匹配器
     */
    private fun getKeywordMatcher(mergedAnnotation: MergedAnnotation<Filter>): (Event) -> Boolean {
        val value = mergedAnnotation.getString("value")
        // 如果 value 为空，则返回 true 匹配器
        if (!StringUtils.hasText(value)) return { true }

        val matchType = mergedAnnotation.getEnum("matchType", MatchType::class.java)
        return { event ->
            if (event !is MessageEvent) {
                false
            } else {
                val msgContent = event.plainText
                when (matchType) {
                    MatchType.EQUALS -> msgContent == value
                    MatchType.EQUALS_IGNORE_CASE -> msgContent.equals(value, ignoreCase = true)
                    MatchType.STARTS_WITH -> msgContent.startsWith(value)
                    MatchType.ENDS_WITH -> msgContent.endsWith(value)
                    MatchType.CONTAINS -> msgContent.contains(value)
                    MatchType.REGEX -> msgContent.matches(value.toRegex())
                    MatchType.REGEX_CONTAINS -> Pattern.compile(value).matcher(msgContent).find()
                }
            }
        }
    }

    /**
     * 获取 Filter 注解中的 Targets 注解匹配器
     *
     * targets 是数组，多个 Targets 之间是"任一满足即可"（OR）；
     * 单个 Targets 内部的 users/groups/mentions/mentionBot 各维度之间是"全部满足"（AND），
     * 某个维度未声明（空数组/false）时视为不限制该维度。
     */
    private fun getTargetMatcher(mergedAnnotation: MergedAnnotation<Filter>): (Event) -> Boolean {
        val targetsAnnotations = mergedAnnotation.getAnnotationArray("targets", Filter.Targets::class.java)
        // 没有声明任何 Targets，则不限制
        if (targetsAnnotations.isEmpty()) return { true }

        val targetMatchers = targetsAnnotations.map { toTargetMatcher(it) }
        return { event -> targetMatchers.any { matcher -> matcher(event) } }
    }

    /**
     * 将单个 Targets 注解转换为匹配器
     */
    private fun toTargetMatcher(targets: MergedAnnotation<Filter.Targets>): (Event) -> Boolean {
        val users = targets.getLongArray("users")
        val groups = targets.getLongArray("groups")
        val mentions = targets.getLongArray("mentions")
        val mentionBot = targets.getBoolean("mentionBot")

        return { event ->
            if (event !is MessageEvent) {
                false
            } else if (users.isNotEmpty() && !users.contains(event.senderId)) {
                false
            } else if (groups.isNotEmpty() && (event !is GroupMessageEvent || !groups.contains(event.peerId))) {
                false
            } else if (mentions.isNotEmpty() && mentions.none { event.mentionedUserIds.contains(it) }) {
                false
            } else if (mentionBot && !event.mentionedUserIds.contains(event.selfId)) {
                false
            } else {
                true
            }
        }
    }
}
