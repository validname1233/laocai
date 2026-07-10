package indi.dkx.laocai.bot.model.event.data;

import indi.dkx.laocai.bot.model.entity.GroupEntity;
import indi.dkx.laocai.bot.model.entity.GroupMemberEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 群消息事件数据。
 * <p>
 * 群消息处理通常同时需要群信息和成员信息，放在同一个对象里更方便直接使用。
 */
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Data
public class IncomingGroupMessage extends IncomingMessage {
    private GroupEntity group;
    private GroupMemberEntity groupMember;
}


