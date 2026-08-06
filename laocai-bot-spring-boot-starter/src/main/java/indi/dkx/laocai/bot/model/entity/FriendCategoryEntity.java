package indi.dkx.laocai.bot.model.entity;

/**
 * 好友分组元数据。
 * <p>
 * 上层只需要分组编号和名称来呈现好友关系，不应该直接依赖原始协议对象。
 * @param categoryId 好友分组编号
 * @param categoryName 好友分组名称
 */
public record FriendCategoryEntity(
        Integer categoryId,
        String categoryName
) { }


