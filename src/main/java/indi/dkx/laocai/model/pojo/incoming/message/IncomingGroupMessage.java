package indi.dkx.laocai.model.pojo.incoming.message;

import indi.dkx.laocai.model.entity.GroupEntity;
import indi.dkx.laocai.model.entity.GroupMemberEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class IncomingGroupMessage extends IncomingMessage {
    private GroupEntity group;
    private GroupMemberEntity groupMember;
}
