package indi.kyson.laocai.bot.annotation

import indi.kyson.laocai.bot.constant.PriorityConstant

@java.lang.annotation.Repeatable(Filters::class)
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Filter(
    val value: String = "",
    val matchType: MatchType = MatchType.REGEX,
    val priority: Int = PriorityConstant.DEFAULT,
    val targets: Array<Targets> = [],
) {
    @Retention(AnnotationRetention.RUNTIME)
    annotation class Targets(
        val users: LongArray = [],
        val groups: LongArray = [],
        val mentions: LongArray = [],
        val mentionBot: Boolean = false,
    )
}
