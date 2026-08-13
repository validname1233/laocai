package indi.kyson.laocai.handler.texas;

import indi.kyson.laocai.bot.annotation.Filter;
import indi.kyson.laocai.bot.annotation.Listener;
import indi.kyson.laocai.bot.core.BotSender;
import indi.kyson.laocai.bot.model.event.Event;
import indi.kyson.laocai.bot.model.event.data.IncomingGroupMessage;
import indi.kyson.laocai.bot.model.segment.TextSegment;
import indi.kyson.laocai.handler.texas.player.TexasPlayer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.ArrayList;
import java.util.Objects;

/**
 * 德州扑克牌局处理器。
 * <p>
 * 牌局状态、报名名单和广播消息需要共享同一份会话上下文，单独收拢在一个处理器里更容易维护。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TexasHandler {

    private final BotSender botSender;
    private final List<TexasPlayer> players = new ArrayList<>();
    private long groupId = 0;

    @Listener
    @Filter("我要玩德州扑克")
    public void texasInit(Event<IncomingGroupMessage> event) {
        IncomingGroupMessage message = event.data();
        log.info("收到群消息: {}", message.getPlainText());
        if(groupId == 0)groupId = message.getGroup().groupId();

        if(!players.isEmpty()){
            boolean inGame = false;
            for(TexasPlayer player : players){
                if (Objects.equals(player.userId, message.getGroupMember().userId())) {
                    inGame = true;
                    break;
                }
            }
            if(!inGame)players.add(new TexasPlayer(message.getGroupMember().nickname(),message.getGroupMember().userId()));
        }
        else{
            players.add(new TexasPlayer(message.getGroupMember().nickname(),message.getGroupMember().userId()));
        }

        StringBuilder temp = new StringBuilder();
        for (TexasPlayer player : players) {
            temp.append(player.nickname).append(" ");
        }
        botSender.sendGroupMsg(groupId, List.of(
            TextSegment.of("当前玩家"),
            TextSegment.of(temp.toString())
        ));
    }

    @Listener
    @Filter("开始德州扑克")
    public void texasStart(Event<IncomingGroupMessage> event) {
        IncomingGroupMessage message = event.data();
        log.info("收到群消息: {}", message.getPlainText());
        StringBuilder temp = new StringBuilder();
        for (TexasPlayer player : players) {
            temp.append(player.nickname).append(" ");
        }
        botSender.sendGroupMsg(groupId, List.of(
            TextSegment.of("当前玩家"),
            TextSegment.of(temp.toString()),
            TextSegment.of("开始游戏")
        ));



        // 准备和胜负判定先留出独立入口，避免把牌局流程直接揉进报名逻辑里。

    }
}


