package indi.dkx.laocai.service.impl;

import indi.dkx.laocai.config.QQBotConfig;
import indi.dkx.laocai.pojo.LoginRequest;
import indi.dkx.laocai.pojo.LoginResponse;
import indi.dkx.laocai.service.LoginService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class LoginServiceImpl implements LoginService {

    @Resource
    private QQBotConfig qqBotConfig;

    @Resource
    private WebClient webClient;

    @Override
    public Mono<LoginResponse> login() {
        log.debug("login");
        return webClient.post().uri("https://bots.qq.com/app/getAppAccessToken")
                .header("Content-Type", "application/json")
                .bodyValue(new LoginRequest(qqBotConfig.getAppid(), qqBotConfig.getSecret()))
                .retrieve()
                .bodyToMono(LoginResponse.class);
    }
}
