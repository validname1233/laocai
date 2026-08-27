package indi.kyson.laocai.handler

import indi.kyson.laocai.bot.Bot
import indi.kyson.laocai.bot.annotation.Filter
import indi.kyson.laocai.bot.annotation.Listener
import indi.kyson.laocai.bot.event.FriendMessageEvent
import indi.kyson.laocai.bot.event.GroupMessageEvent
import indi.kyson.laocai.bot.segment.MentionSegment
import indi.kyson.laocai.bot.segment.OutgoingRecordSegment
import indi.kyson.laocai.bot.segment.TextSegment
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class TestHandler(
    private val bot: Bot,
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @Listener
    @Filter(
        value = "^[1-9]\\d{4,11}@qq\\.com(\\.cn)?$",
        targets = [Filter.Targets(groups = [634550174L])],
    )
    fun handleGroup(event: GroupMessageEvent) {
        log.info("收到群消息: {}", event.plainText)
        if (event.senderId == 1938437495L) {
            bot.sendGroupMsg(
                event.group.groupId,
                listOf(
                    MentionSegment.of(event.senderId),
                    TextSegment.of(" 哈！"),
                ),
            ).block()
        } else {
            bot.sendGroupMsg(
                event.group.groupId,
                listOf(
                    MentionSegment.of(event.senderId),
                    TextSegment.of(" 喵"),
                ),
            ).block()
        }
    }

    @Listener
    fun handleFriend(event: FriendMessageEvent) {
        log.info("收到好友消息: {}", event.plainText)
        if (event.plainText == "D") {
            Thread.sleep(5000L)
        }
        bot.sendPrivateMsg(
            event.senderId,
            listOf(OutgoingRecordSegment.of("file://D:\\QQbot\\laocai-llbot-java\\tmp\\audios\\output_ja.wav")),
        ).block()
    }
}
