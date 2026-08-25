package indi.kyson.laocai.bot.annotation

import indi.kyson.laocai.bot.constant.PriorityConstant

@java.lang.annotation.Repeatable(MultiFilters::class)
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class MultiFilter(
    val value: Array<Filter>,
    val type: Type = Type.ANY,
    val priority: Int = PriorityConstant.DEFAULT,
) {
    enum class Type {
        ANY,
        ALL,
        NONE,
    }
}
