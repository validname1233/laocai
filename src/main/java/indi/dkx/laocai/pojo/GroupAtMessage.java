package indi.dkx.laocai.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GroupAtMessage {
    private String id;
    private String content;
    private String timestamp;
    private Object author;
    private String group_openid;
    private List<Object> attachments;
    private String group_id;
    private Object message_scene;
    private Integer message_type;
}
