package indi.dkx.laocai.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginRequest {
    private String appId;
    private String clientSecret;
}
