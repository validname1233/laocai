package indi.kyson.laocai.app.conversation

/** Application-facing boundary for generating a reply without coupling callers to an LLM API. */
fun interface GroupReplyGenerator {
    fun generate(context: GroupConversationContext): String
}
