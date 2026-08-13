package indi.kyson.laocai.bot.core.entity

import indi.kyson.laocai.bot.core.enums.Sex

data class FriendEntity(
    val userId: Long,
    val nickname: String,
    val sex: Sex,
    val qid: String,
    val remark: String,
    val category: FriendCategoryEntity,
)
