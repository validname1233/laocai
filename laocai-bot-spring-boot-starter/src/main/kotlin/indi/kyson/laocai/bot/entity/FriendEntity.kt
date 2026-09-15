package indi.kyson.laocai.bot.entity

import com.fasterxml.jackson.annotation.JsonProperty
import indi.kyson.laocai.bot.enums.Sex

data class FriendEntity(
    @JsonProperty("user_id")
    val userId: Long,
    val nickname: String,
    val sex: Sex,
    val qid: String,
    val remark: String,
    val category: FriendCategoryEntity,
)
