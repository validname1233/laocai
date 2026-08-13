package indi.kyson.laocai.bot.core.response

import indi.kyson.laocai.bot.core.enums.ResponseStatus

data class Response<T>(
    val status: ResponseStatus,
    val retcode: Long,
    val data: T,
    val message: String,
)
