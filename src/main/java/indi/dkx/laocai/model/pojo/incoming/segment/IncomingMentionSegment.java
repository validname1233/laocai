package indi.dkx.laocai.model.pojo.incoming.segment;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * mention 提及消息段
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class IncomingMentionSegment extends IncomingSegment {
    /**
     * 被提及的用户 QQ号
     */
    private Long userId;
}
