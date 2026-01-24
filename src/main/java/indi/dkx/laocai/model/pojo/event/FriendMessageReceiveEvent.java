package indi.dkx.laocai.model.pojo.event;

import indi.dkx.laocai.model.pojo.message.IncomingFriendMessage;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Data
public class FriendMessageReceiveEvent extends Event<IncomingFriendMessage> {
}
