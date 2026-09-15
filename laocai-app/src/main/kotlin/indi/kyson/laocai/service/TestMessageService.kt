package indi.kyson.laocai.service

import indi.kyson.laocai.bot.Bot
import indi.kyson.laocai.bot.event.FriendMessageEvent
import indi.kyson.laocai.bot.event.GroupMessageEvent
import indi.kyson.laocai.bot.segment.MentionSegment
import indi.kyson.laocai.bot.segment.OutgoingRecordSegment
import indi.kyson.laocai.bot.segment.TextSegment
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class TestMessageService(private val bot: Bot) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun handleGroup(event: GroupMessageEvent) {
        log.info("收到群消息: {}", event.plainText)
        val text = if (event.senderId == 1938437495L) " 哈！" else " 喵"
        bot.sendGroupMsg(event.group.groupId, listOf(MentionSegment.of(event.senderId), TextSegment.of(text))).block()
    }

    fun handleFriend(event: FriendMessageEvent) {
        log.info("收到好友消息: {}", event.plainText)
        if (event.plainText == "D") Thread.sleep(5000L)
        bot.sendPrivateMsg(event.senderId, listOf(OutgoingRecordSegment.of("file://D:\\QQbot\\laocai-llbot-java\\tmp\\audios\\output_ja.wav"))).block()
    }
}
