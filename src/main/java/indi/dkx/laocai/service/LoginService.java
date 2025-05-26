package indi.dkx.laocai.service;

import indi.dkx.laocai.pojo.LoginResponse;
import reactor.core.publisher.Mono;

public interface LoginService {
    Mono<LoginResponse> login();
}
