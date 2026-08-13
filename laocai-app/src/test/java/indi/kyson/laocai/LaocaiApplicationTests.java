package indi.kyson.laocai;

import indi.kyson.laocai.bot.annotation.EnableLaocaiBot;
import org.junit.jupiter.api.Test;
import org.springframework.core.annotation.AnnotatedElementUtils;

import static org.assertj.core.api.Assertions.assertThat;

class LaocaiApplicationTests {

    @Test
    void applicationEnablesLaocaiBot() {
        assertThat(AnnotatedElementUtils.hasAnnotation(LaocaiApplication.class, EnableLaocaiBot.class))
                .isTrue();
    }
}
