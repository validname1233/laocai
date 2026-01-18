package indi.dkx.laocai.game_handler;

import indi.dkx.laocai.annotation.Filter;
import indi.dkx.laocai.annotation.Listener;
import indi.dkx.laocai.core.BotSender;
import indi.dkx.laocai.model.pojo.incoming.message.IncomingFriendMessage;
import indi.dkx.laocai.model.pojo.incoming.message.IncomingGroupMessage;
import indi.dkx.laocai.model.pojo.incoming.segment.IncomingMentionSegment;
import indi.dkx.laocai.model.pojo.incoming.segment.IncomingTextSegment;
import indi.dkx.laocai.game_handler.player.TexasPlayer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.ArrayList;

@Slf4j
@Component
@RequiredArgsConstructor
public class Texas{

    private final BotSender botSender;
    private List<TexasPlayer> players = new ArrayList<>();
    private long groupId = 0;

    @Listener
    @Filter("我要玩德州扑克")
    public void texasInit(IncomingGroupMessage message) {
        log.info("收到群消息: {}", message.getPlainText());
        if(groupId == 0)groupId = message.getGroup().groupId();

        if(!players.isEmpty()){
            boolean inGame = false;
            for(TexasPlayer player : players){
                if (player.userId == message.getGroupMember().userId()) {
                    inGame = true;
                    break;
                }
            }
            if(!inGame)players.add(new TexasPlayer(message.getGroupMember().nickname(),message.getGroupMember().userId()));
        }
        else{
            players.add(new TexasPlayer(message.getGroupMember().nickname(),message.getGroupMember().userId()));
        }

        String temp = "";
        for (TexasPlayer player : players) {
            temp += player.nickname + " ";
        }
        botSender.sendGroupMsg(groupId, List.of(
            IncomingTextSegment.of("当前玩家"),
            IncomingTextSegment.of(temp)
        ));
    }

    @Listener
    @Filter("开始德州扑克")
    public void texasStart(IncomingGroupMessage message) {
        log.info("收到群消息: {}", message.getPlainText());
        String temp = "";
        for (TexasPlayer player : players) {
            temp += player.nickname + " ";
        }
        botSender.sendGroupMsg(groupId, List.of(
            IncomingTextSegment.of("当前玩家"),
            IncomingTextSegment.of(temp),
            IncomingTextSegment.of("开始游戏")
        ));


        
        //准备阶段及胜负判定

    }
}
