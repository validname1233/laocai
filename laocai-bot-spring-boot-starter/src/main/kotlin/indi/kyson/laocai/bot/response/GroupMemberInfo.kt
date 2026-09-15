package indi.kyson.laocai.bot.response

import com.fasterxml.jackson.annotation.JsonProperty
import indi.kyson.laocai.bot.entity.GroupMemberEntity

data class GroupMemberInfo(
    @JsonProperty("member")
    val member: GroupMemberEntity
)
