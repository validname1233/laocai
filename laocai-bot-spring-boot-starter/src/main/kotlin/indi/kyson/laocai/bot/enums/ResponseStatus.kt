package indi.kyson.laocai.bot.enums

import com.fasterxml.jackson.annotation.JsonValue

enum class ResponseStatus(@get:JsonValue val value: String) {
    OK("ok"),
    FAILED("failed"),
}
