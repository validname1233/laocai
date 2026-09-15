package indi.kyson.laocai.service

import indi.kyson.laocai.ai.ChatClientFactory
import indi.kyson.laocai.ai.GPTSoVITSClient
import indi.kyson.laocai.bot.Bot
import indi.kyson.laocai.bot.event.GroupMessageEvent
import indi.kyson.laocai.bot.segment.OutgoingRecordSegment
import indi.kyson.laocai.bot.segment.TextSegment
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class AudioReplyService(
    private val chatClientFactory: ChatClientFactory,
    private val bot: Bot,
    private val ttsClient: GPTSoVITSClient,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun handle(event: GroupMessageEvent) {
        val groupId = event.group.groupId
        val prompt = event.plainText.substring(AUDIO_COMMAND.length).trim()
        log.info("收到 /audio 命令: groupId={} prompt={}", groupId, prompt)
        if (prompt.isEmpty()) {
            bot.sendGroupMsg(groupId, listOf(TextSegment.of("用法：/audio 你想让洛琪希说的话"))).block()
            return
        }
        val reply = chatClientFactory.getRoxyVoiceClient(groupId).prompt().user(prompt).call().content()
        if (reply.isNullOrBlank()) {
            log.error("洛琪希人格回复为空: groupId={}", groupId)
            bot.sendGroupMsg(groupId, listOf(TextSegment.of("生成回复失败了，再试一次吧"))).block()
            return
        }
        val trimmed = reply.trim()
        log.info("洛琪希回复: {}", trimmed)
        val audio = ttsClient.synthesize(trimmed).block()
        if (audio == null) {
            bot.sendGroupMsg(groupId, listOf(TextSegment.of("语音合成失败了，先把文字给你：\n$trimmed"))).block()
            return
        }
        bot.sendGroupMsg(groupId, listOf(OutgoingRecordSegment.of("file://$audio"))).block()
    }

    companion object { private const val AUDIO_COMMAND = "/audio" }
}
