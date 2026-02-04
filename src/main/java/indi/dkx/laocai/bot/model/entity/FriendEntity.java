package indi.dkx.laocai.bot.model.entity;

import indi.dkx.laocai.bot.model.enums.Sex;

/**
 * 好友实体
 * @param userId 用户 QQ 号
 * @param nickname 用户昵称
 * @param sex 用户性别，可能值：male female unknown
 * @param qid 用户 QID
 * @param remark 好友备注
 * @param category 好友分组
 */
public record FriendEntity(
        Long userId,
        String nickname,
        Sex sex,
        String qid,
        String remark,
        FriendCategoryEntity category
) { }
