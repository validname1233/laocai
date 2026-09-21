package indi.kyson.laocai.app.conversation

/** AI 可读的历史文本，以及按全局图片编号排列的资源引用。 */
data class RenderedHistory(
    val text: String,
    val imageResources: List<String>,
)
