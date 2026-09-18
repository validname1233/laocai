package indi.kyson.laocai.bot.event

/**
 * 机器人离线事件
 */
data class BotOfflineEvent(
    override val time: Long,
    override val selfId: Long,
    val reason: String,
) : Event