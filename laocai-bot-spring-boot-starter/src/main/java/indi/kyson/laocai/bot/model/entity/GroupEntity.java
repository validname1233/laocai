package indi.kyson.laocai.bot.model.entity;

/**
 * 群信息快照。
 * <p>
 * 事件处理和展示层只关心群的标识与容量信息，不需要直接依赖协议返回对象。
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


