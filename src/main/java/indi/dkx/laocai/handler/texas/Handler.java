package indi.dkx.laocai.handler.texas;

import indi.dkx.laocai.bot.annotation.Filter;
import indi.dkx.laocai.bot.annotation.Listener;
import indi.dkx.laocai.bot.core.BotSender;
import indi.dkx.laocai.bot.model.event.Event;
import indi.dkx.laocai.bot.model.event.data.IncomingGroupMessage;
import indi.dkx.laocai.bot.model.segment.TextSegment;
import indi.dkx.laocai.handler.texas.player.TexasPlayer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.ArrayList;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class Handler {

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
            new TextSegment("当前玩家"),
            new TextSegment(temp.toString())
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
            new TextSegment("当前玩家"),
            new TextSegment(temp.toString()),
            new TextSegment("开始游戏")
        ));


        
        //准备阶段及胜负判定

    }
}
