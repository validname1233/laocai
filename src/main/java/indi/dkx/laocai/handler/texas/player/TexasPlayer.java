package indi.dkx.laocai.handler.texas.player;

/**
 * 德州扑克牌局中的具体玩家类型。
 * <p>
 * 保留一个独立子类，后续如果要给牌局玩家加状态字段，不用改基础身份对象。
 */
public class TexasPlayer extends Player{
    public TexasPlayer(String nickname, Long userId) {
        super(nickname, userId);
    }
}


