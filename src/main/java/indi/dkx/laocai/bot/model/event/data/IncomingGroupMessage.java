package indi.dkx.laocai.bot.model.event.data;

import indi.dkx.laocai.bot.model.entity.GroupEntity;
import indi.dkx.laocai.bot.model.entity.GroupMemberEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Data
public class IncomingGroupMessage extends IncomingMessage {
    private GroupEntity group;
    private GroupMemberEntity groupMember;
}
