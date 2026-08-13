package indi.kyson.laocai.bot.core.annotation

import indi.kyson.laocai.bot.core.constant.PriorityConstant

@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.ANNOTATION_CLASS)
annotation class Listener(
    val id: String = "",
    val priority: Int = PriorityConstant.DEFAULT,
)
