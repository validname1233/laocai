package indi.dkx.laocai.bot.model.event.data;

import indi.dkx.laocai.bot.model.entity.FriendEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Data
public class IncomingFriendMessage extends IncomingMessage {
    private FriendEntity friend;
}
