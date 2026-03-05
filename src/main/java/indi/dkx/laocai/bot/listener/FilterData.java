package indi.dkx.laocai.bot.listener;

import indi.dkx.laocai.bot.model.event.Event;

import java.util.function.Predicate;

public record FilterData(
        int priority,
        Predicate<Event<?>> matcher
) {
}
