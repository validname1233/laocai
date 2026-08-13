package indi.kyson.laocai.bot.core.entity

import indi.kyson.laocai.bot.core.enums.Role
import indi.kyson.laocai.bot.core.enums.Sex

data class GroupMemberEntity(
    val userId: Long,
    val nickname: String,
    val sex: Sex,
    val groupId: Long,
    val card: String,
    val title: String,
    val level: Int,
    val role: Role,
    val joinTime: Long,
    val lastSentTime: Long,
    val shutUpEndTime: Long?,
)
