package indi.kyson.laocai.bot.response

import indi.kyson.laocai.bot.enums.Sex

data class UserProfile(
    val nickname: String,
    val qid: String,
    val age: Int,
    val sex: Sex,
    val remark: String,
    val bio: String,
    val level: Int,
    val country: String,
    val city: String,
    val school: String,
)
