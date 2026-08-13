package indi.kyson.laocai.bot.annotation

import indi.kyson.laocai.bot.configuration.LaocaiBotConfiguration
import org.springframework.context.annotation.Import

/**
 * 启用机器人相关配置。
 *
 * 把机器人组件的装配入口集中在一个注解上，调用方只需要在启动类上显式声明即可。
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@Import(LaocaiBotConfiguration::class)
annotation class EnableLaocaiBot
