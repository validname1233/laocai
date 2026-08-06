package indi.dkx.laocai.bot.annotation;

import indi.dkx.laocai.bot.configuration.LaocaiBotConfigurationProperties;
import indi.dkx.laocai.bot.application.LaocaiBotRunner;
import indi.dkx.laocai.bot.core.BotSender;
import indi.dkx.laocai.bot.listener.EventDispatcher;
import indi.dkx.laocai.bot.listener.EventListenerProcessor;
import indi.dkx.laocai.bot.listener.EventListenerResolver;
import indi.dkx.laocai.bot.listener.EventListenerResolverRegistryProcessor;
import indi.dkx.laocai.bot.model.event.Event;
import indi.dkx.laocai.bot.model.event.data.IncomingFriendMessage;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import static org.assertj.core.api.Assertions.assertThat;

class EnableLaocaiBotTests {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withPropertyValues(
                    "laocai.bot.url=http://localhost:8080",
                    "laocai.bot.access-token=test-token",
                    "laocai.dispatcher.concurrency=1",
                    "laocai.dispatcher.buffer-size=8"
            );

    @Test
    void enableAnnotationRegistersCompleteBotInfrastructure() {
        contextRunner
                .withUserConfiguration(EnabledConfiguration.class)
                .run(context -> {
                    assertThat(context).hasSingleBean(LaocaiBotConfigurationProperties.class)
                            .hasSingleBean(WebClient.class)
                            .hasSingleBean(BotSender.class)
                            .hasSingleBean(EventDispatcher.class)
                            .hasSingleBean(EventListenerProcessor.class)
                            .hasSingleBean(EventListenerResolverRegistryProcessor.class)
                            .hasSingleBean(LaocaiBotRunner.class);
                    assertThat(context.getBeansOfType(EventListenerResolver.class)).hasSize(1);
                });
    }

    @Test
    void botInfrastructureIsNotScannedWithoutEnableAnnotation() {
        contextRunner
                .withUserConfiguration(WithoutEnableConfiguration.class)
                .run(context -> {
                    assertThat(context).doesNotHaveBean(LaocaiBotConfigurationProperties.class)
                            .doesNotHaveBean(WebClient.class)
                            .doesNotHaveBean(BotSender.class)
                            .doesNotHaveBean(EventDispatcher.class)
                            .doesNotHaveBean(EventListenerProcessor.class)
                            .doesNotHaveBean(EventListenerResolverRegistryProcessor.class)
                            .doesNotHaveBean(LaocaiBotRunner.class)
                            .doesNotHaveBean(EventListenerResolver.class);
                });
    }

    @Configuration(proxyBeanMethods = false)
    @EnableLaocaiBot
    static class EnabledConfiguration {

        @Bean
        WebClient.Builder webClientBuilder() {
            return WebClient.builder();
        }

        @Bean
        TestHandler testHandler(BotSender botSender) {
            return new TestHandler(botSender);
        }
    }

    @Configuration(proxyBeanMethods = false)
    @ComponentScan(
            basePackages = "indi.dkx.laocai.bot",
            includeFilters = @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = Component.class),
            excludeFilters = @ComponentScan.Filter(
                    type = FilterType.ASSIGNABLE_TYPE,
                    classes = EnabledConfiguration.class
            ),
            useDefaultFilters = false
    )
    static class WithoutEnableConfiguration {
    }

    static class TestHandler {

        private final BotSender botSender;

        TestHandler(BotSender botSender) {
            this.botSender = botSender;
        }

        @Listener
        void handle(Event<IncomingFriendMessage> event) {
            // The method only proves that a consumer Handler can be discovered.
            botSender.sendPrivateMsg(event.data().getSenderId(), java.util.List.of());
        }
    }
}
