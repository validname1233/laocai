package indi.dkx.laocai.model.pojo.incoming.segment;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * file 文件消息段
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class IncomingFileSegment extends IncomingSegment {
    /**
     * 文件 ID
     */
    private String fileId;

    /**
     * 文件名称
     */
    private String fileName;

    /**
     * 文件大小（字节）
     * 注意：对应 int64，必须用 Long
     */
    private Long fileSize;

    /**
     * 文件的 TriSHA1 哈希值
     * (Optional) 仅私聊有，群聊可能为 null
     */
    private String fileHash;
}
