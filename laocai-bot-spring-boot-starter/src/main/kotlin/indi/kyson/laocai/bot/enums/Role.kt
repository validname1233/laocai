package indi.kyson.laocai.bot.enums

import com.fasterxml.jackson.annotation.JsonValue

enum class Role(@get:JsonValue val value: String) {
    OWNER("owner"),
    ADMIN("admin"),
    MEMBER("member"),
}
