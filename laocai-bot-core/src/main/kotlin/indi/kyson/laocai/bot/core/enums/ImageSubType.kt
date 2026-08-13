package indi.kyson.laocai.bot.core.enums

import com.fasterxml.jackson.annotation.JsonValue

enum class ImageSubType(@get:JsonValue val value: String) {
    NORMAL("normal"),
    STICKER("sticker"),
}
