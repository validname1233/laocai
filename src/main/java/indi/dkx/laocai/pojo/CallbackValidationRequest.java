package indi.dkx.laocai.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CallbackValidationRequest {
    private String plain_token;
    private String event_ts;
}
