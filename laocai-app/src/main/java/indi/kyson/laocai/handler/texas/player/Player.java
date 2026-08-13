package indi.kyson.laocai.handler.texas.player;

/**
 * 德州扑克牌局参与者的基础信息。
 * <p>
 * Texas 相关逻辑只需要一个最小玩家身份载体，不需要再拆出更多层级。
 */
public class Player {
    public String nickname;
    public Long userId;
    public Player(String nickname, Long userId){
        this.nickname = nickname;
        this.userId = userId;
    }
}


