package indi.kyson.laocai.bot.entity

data class GroupEntity(
    val groupId: Long,
    val groupName: String,
    val memberCount: Int,
    val maxMemberCount: Int,
)
