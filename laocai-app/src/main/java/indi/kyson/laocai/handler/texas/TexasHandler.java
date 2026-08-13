package indi.kyson.laocai.handler.texas;

import indi.kyson.laocai.bot.core.Bot;
import indi.kyson.laocai.bot.core.annotation.Filter;
import indi.kyson.laocai.bot.core.annotation.Listener;
import indi.kyson.laocai.bot.core.event.GroupMessageEvent;
import indi.kyson.laocai.bot.core.segment.TextSegment;
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

    private final Bot bot;
    private final List<TexasPlayer> players = new ArrayList<>();
    private long groupId = 0;

    @Listener
    @Filter("我要玩德州扑克")
    public void texasInit(GroupMessageEvent event) {
        log.info("收到群消息: {}", event.getPlainText());
        if(groupId == 0)groupId = event.getGroup().getGroupId();

        if(!players.isEmpty()){
            boolean inGame = false;
            for(TexasPlayer player : players){
                if (Objects.equals(player.userId, event.getGroupMember().getUserId())) {
                    inGame = true;
                    break;
                }
            }
            if(!inGame)players.add(new TexasPlayer(event.getGroupMember().getNickname(),event.getGroupMember().getUserId()));
        }
        else{
            players.add(new TexasPlayer(event.getGroupMember().getNickname(),event.getGroupMember().getUserId()));
        }

        StringBuilder temp = new StringBuilder();
        for (TexasPlayer player : players) {
            temp.append(player.nickname).append(" ");
        }
        bot.sendGroupMsg(groupId, List.of(
            TextSegment.of("当前玩家"),
            TextSegment.of(temp.toString())
        ));
    }

    @Listener
    @Filter("开始德州扑克")
    public void texasStart(GroupMessageEvent event) {
        log.info("收到群消息: {}", event.getPlainText());
        StringBuilder temp = new StringBuilder();
        for (TexasPlayer player : players) {
            temp.append(player.nickname).append(" ");
        }
        bot.sendGroupMsg(groupId, List.of(
            TextSegment.of("当前玩家"),
            TextSegment.of(temp.toString()),
            TextSegment.of("开始游戏")
        ));



        // 准备和胜负判定先留出独立入口，避免把牌局流程直接揉进报名逻辑里。

    }
}

