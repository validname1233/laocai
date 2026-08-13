package indi.kyson.laocai.bot.core.enums

import com.fasterxml.jackson.annotation.JsonValue

enum class Sex(@get:JsonValue val value: String) {
    MALE("male"),
    FEMALE("female"),
    UNKNOWN("unknown"),
}
