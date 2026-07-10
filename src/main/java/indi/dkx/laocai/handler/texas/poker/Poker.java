package indi.dkx.laocai.handler.texas.poker;

/**
 * 单张扑克牌。
 * <p>
 * 牌面、花色和显示名经常一起使用，拆散后反而会让牌堆构建更难读。
 */
public record Poker(Suit suit, Rank rank, String name) {}



