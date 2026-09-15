package indi.kyson.laocai.bot.entity

import com.fasterxml.jackson.annotation.JsonProperty

data class GroupEntity(
    @JsonProperty("group_id")
    val groupId: Long,
    @JsonProperty("group_name")
    val groupName: String,
    @JsonProperty("member_count")
    val memberCount: Int,
    @JsonProperty("max_member_count")
    val maxMemberCount: Int,
)
