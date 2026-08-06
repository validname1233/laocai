package indi.dkx.laocai.bot.model.event.data;

import indi.dkx.laocai.bot.model.entity.FriendEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 好友消息事件数据。
 * <p>
 * 好友消息比公共基类多了好友关系信息，便于在处理器里直接读取展示字段。
 */
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Data
public class IncomingFriendMessage extends IncomingMessage {
    private FriendEntity friend;
}


