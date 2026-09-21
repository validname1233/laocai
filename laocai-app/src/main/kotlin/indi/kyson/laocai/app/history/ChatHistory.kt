package indi.kyson.laocai.app.history

import indi.kyson.laocai.bot.segment.Segment
import tools.jackson.databind.annotation.JsonDeserialize

data class ChatHistory(
    val time: Long,
    val senderId: Long,
    val segments: List<Segment>,
)
