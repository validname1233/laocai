package indi.dkx.laocai.bot.model.entity;

/**
 * 群实体
 * @param groupId 群号
 * @param groupName 群名称
 * @param memberCount 群成员数量
 * @param maxMemberCount 群容量
 */
public record GroupEntity(
        Long groupId,
        String groupName,
        Integer memberCount,
        Integer maxMemberCount
) { }
