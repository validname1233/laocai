package indi.kyson.laocai.bot.response

import indi.kyson.laocai.bot.enums.ResponseStatus

data class Response<T>(
    val status: ResponseStatus,
    val retcode: Long,
    val data: T?,
    val message: String?,
)
