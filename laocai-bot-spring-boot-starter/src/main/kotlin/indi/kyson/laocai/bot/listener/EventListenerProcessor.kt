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
     * 这里使用 JDK 的 getAnnotationsByType，而不是 Spring MergedAnnotations.stream：
     * MultiFilter 的 value 本身包含 Filter 注解，后者不会被 MergedAnnotations 作为方法上的
     * MultiFilter 发现，导致 NONE 过滤器被静默跳过。
     *
     * 单独重复的 @Filter 之间是"全部满足"（AND）；@MultiFilter 内部按其 type 声明的
     * ANY/ALL/NONE 逻辑组合一组 @Filter，作为整体再参与外层的 AND 组合。
     */
    private fun getFilterDataList(method: Method): List<FilterData> {
        val result = mutableListOf<FilterData>()

        method.getAnnotationsByType(Filter::class.java).forEach { filter ->
            result.add(FilterData(filter.priority, toFilterMatcher(filter)))
        }

        method.getAnnotationsByType(MultiFilter::class.java).forEach { multiFilter ->
            result.add(FilterData(multiFilter.priority, toMultiFilterMatcher(multiFilter)))
        }

        return result
    }

    /**
     * 将单个 Filter 注解转换为匹配器（关键词匹配 + Targets 匹配同时满足）
     */
    private fun toFilterMatcher(filter: Filter): (Event) -> Boolean {
        val keywordMatcher = getKeywordMatcher(filter)
        val targetMatcher = getTargetMatcher(filter)
        return { event -> keywordMatcher(event) && targetMatcher(event) }
    }

    /**
     * 将 MultiFilter 注解转换为匹配器，按其 type 组合内部的一组 Filter
     */
    private fun toMultiFilterMatcher(multiFilter: MultiFilter): (Event) -> Boolean {
        val subMatchers = multiFilter.value.map { toFilterMatcher(it) }

        return when (multiFilter.type) {
            MultiFilter.Type.ANY -> { event -> subMatchers.any { it(event) } }
            MultiFilter.Type.ALL -> { event -> subMatchers.all { it(event) } }
            MultiFilter.Type.NONE -> { event -> subMatchers.none { it(event) } }
        }
    }

    /**
     * 获取 Filter 注解中的 String value 匹配器
     */
    private fun getKeywordMatcher(filter: Filter): (Event) -> Boolean {
        val value = filter.value
        // 如果 value 为空，则返回 true 匹配器
        if (!StringUtils.hasText(value)) return { true }

        return { event ->
            if (event !is MessageEvent) {
                false
            } else {
                val msgContent = event.plainText
                when (filter.matchType) {
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
    private fun getTargetMatcher(filter: Filter): (Event) -> Boolean {
        val targets = filter.targets
        // 没有声明任何 Targets，则不限制
        if (targets.isEmpty()) return { true }

        val targetMatchers = targets.map { toTargetMatcher(it) }
        return { event -> targetMatchers.any { matcher -> matcher(event) } }
    }

    /**
     * 将单个 Targets 注解转换为匹配器
     */
    private fun toTargetMatcher(targets: Filter.Targets): (Event) -> Boolean {
        val users = targets.users
        val groups = targets.groups
        val mentions = targets.mentions
        val mentionBot = targets.mentionBot

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
