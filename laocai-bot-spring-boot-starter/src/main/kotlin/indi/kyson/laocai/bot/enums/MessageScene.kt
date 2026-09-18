package indi.kyson.laocai.bot.enums

import com.fasterxml.jackson.annotation.JsonValue

/**
 * 消息场景。
 */
enum class MessageScene(@get:JsonValue val value: String) {
    FRIEND("friend"),
    GROUP("group"),
    TEMP("temp");

    companion object {
        fun fromValue(value: String): MessageScene = entries.firstOrNull { it.value == value }
            ?: throw IllegalArgumentException("Unknown message_scene: $value")
    }
}