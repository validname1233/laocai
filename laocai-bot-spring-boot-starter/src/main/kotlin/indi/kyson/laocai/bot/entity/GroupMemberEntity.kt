package indi.kyson.laocai.bot.entity

import com.fasterxml.jackson.annotation.JsonProperty
import indi.kyson.laocai.bot.enums.Role
import indi.kyson.laocai.bot.enums.Sex

data class GroupMemberEntity(
    @JsonProperty("user_id")
    val userId: Long,
    val nickname: String,
    val sex: Sex,
    @JsonProperty("group_id")
    val groupId: Long,
    val card: String,
    val title: String,
    val level: Int,
    val role: Role,
    @JsonProperty("join_time")
    val joinTime: Long,
    @JsonProperty("last_sent_time")
    val lastSentTime: Long,
    @JsonProperty("shut_up_end_time")
    val shutUpEndTime: Long?,
)
