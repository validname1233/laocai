package indi.dkx.laocai.model.pojo.incoming.message;

import indi.dkx.laocai.model.entity.FriendEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Data
public class IncomingFriendMessage extends IncomingMessage {
    private FriendEntity friend;
}
