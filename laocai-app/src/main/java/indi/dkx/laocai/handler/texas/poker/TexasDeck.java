package indi.dkx.laocai.handler.texas.poker;

/**
 * 德州牌局的派牌与判定骨架。
 * <p>
 * Texas 牌局既要复用牌堆，又要保留玩家数量和组合判定入口，所以单独扩一层。
 */
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

    /**
     * 牌型判定入口。
     * <p>
     * 先保留一个统一入口，后面补完整比较规则时不需要改调用方式。
     */
    public int Judge(){
        int maxComb = 0;
        return 0;
    }

}

