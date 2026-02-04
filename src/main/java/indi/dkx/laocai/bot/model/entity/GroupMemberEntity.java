package indi.dkx.laocai.bot.model.entity;

import indi.dkx.laocai.bot.model.enums.Role;
import indi.dkx.laocai.bot.model.enums.Sex;

/**
 * 群成员实体
 * @param userId 用户 QQ 号
 * @param nickname 用户昵称
 * @param sex 用户性别，可能值：male female unknown
 * @param groupId 群号
 * @param card 成员备注
 * @param title 专属头衔
 * @param level 群等级，注意和 QQ 等级区分
 * @param role 权限等级，可能值：owner admin member
 * @param joinTime 入群时间，Unix 时间戳（秒）
 * @param lastSentTime 最后发言时间，Unix 时间戳（秒）
 * @param shutUpEndTime 禁言结束时间，Unix 时间戳（秒）
 */
public record GroupMemberEntity(
        Long userId,
        String nickname,
        Sex sex,
        Long groupId,
        String card,
        String title,
        Integer level,
        Role role,
        Long joinTime,
        Long lastSentTime,
        Long shutUpEndTime
) { }
