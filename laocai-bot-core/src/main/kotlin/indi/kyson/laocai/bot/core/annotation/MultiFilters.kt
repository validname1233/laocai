package indi.kyson.laocai.bot.core.annotation

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class MultiFilters(val value: Array<MultiFilter>)
