package indi.kyson.laocai.bot.model.entity;

import indi.kyson.laocai.bot.model.enums.Sex;

/**
 * 好友信息快照。
 * <p>
 * 把好友展示所需字段收拢成一个不可变记录，避免业务层到处传递协议原始结构。
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


