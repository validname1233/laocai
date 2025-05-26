package indi.dkx.laocai.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GroupRequest {
    private String content;
    private Byte msg_type;
    private String msg_id;
    private Integer msg_seq;
    private Object markdown;
    private Object media;
    private Object ark;
}
