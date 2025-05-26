package indi.dkx.laocai.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Payload {
    // payload id
    private String id;
    private byte op;
    private Object d;
    private Integer s;
    private String t;
}
