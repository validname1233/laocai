package indi.dkx.laocai.model.entity;

/**
 * 好友分组实体
 * @param categoryId 好友分组ID
 * @param categoryName 好友分组名称
 */
public record FriendCategoryEntity(
        Integer categoryId,
        String categoryName
) { }
