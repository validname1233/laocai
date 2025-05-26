package indi.dkx.laocai.service;

import indi.dkx.laocai.pojo.GroupAtMessage;
import indi.dkx.laocai.pojo.GroupResponse;
import reactor.core.publisher.Mono;

public interface GroupAtMessageService {
    Mono<GroupResponse> handle(GroupAtMessage groupAtMessage);
}
