package indi.dkx.laocai.model.pojo.event;

import indi.dkx.laocai.model.pojo.incoming.message.IncomingMessage;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Data
public class IncomingMessageEvent<T extends IncomingMessage> extends Event {
    private T data;
}
