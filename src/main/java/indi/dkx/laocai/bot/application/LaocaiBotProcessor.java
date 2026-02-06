package indi.dkx.laocai.bot.application;

import indi.dkx.laocai.bot.listener.EventDispatcher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LaocaiBotProcessor {

    private final EventDispatcher eventDispatcher;

}
