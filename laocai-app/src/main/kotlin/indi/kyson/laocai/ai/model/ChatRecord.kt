package indi.kyson.laocai.ai.model

data class ChatRecord(
    val time: Long,
    val senderId: Long,
    val content: String,
    val imageIds: List<String>,
)
