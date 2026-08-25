package indi.kyson.laocai.bot.annotation

import indi.kyson.laocai.bot.constant.PriorityConstant

@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.ANNOTATION_CLASS)
annotation class Listener(
    val id: String = "",
    val priority: Int = PriorityConstant.DEFAULT,
)
