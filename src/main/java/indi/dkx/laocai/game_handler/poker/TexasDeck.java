package indi.dkx.laocai.game_handler.poker;
import java.util.*;

import indi.dkx.laocai.game_handler.poker.Deck;

public class TexasDeck extends Deck{
    private final int playerNum;
    private int[] combination;
    public TexasDeck(int playerNum){
        this.playerNum = playerNum;
        combination = new int[playerNum];
    }

    public int drawHoleCards(){
        return 0;
    }

    public int drawCommunityCards(){
        return 0;
    }

    //先判断牌型大小，若最大牌型者牌型相同，则进入同牌型判断大小逻辑
    //首先定义最大牌型，遍历所有玩家牌型，
    public int Judge(){
        int maxComb = 0;
        return 0;
    }
    
}