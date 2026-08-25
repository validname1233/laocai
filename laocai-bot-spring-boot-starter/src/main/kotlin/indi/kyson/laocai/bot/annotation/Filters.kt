package indi.kyson.laocai.bot.annotation

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class Filters(val value: Array<Filter>)
