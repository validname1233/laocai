package indi.dkx.laocai.bot.listener;

import indi.dkx.laocai.bot.annotation.ApplyBinder;
import indi.dkx.laocai.bot.annotation.Listener;
import indi.dkx.laocai.bot.binder.BinderManager;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.aop.scope.ScopedProxyUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.function.Supplier;

/**
 * 在 BeanFactory 后处理阶段扫描所有 Bean，寻找 @Listener 方法，并为其生成 EventListenerResolver Bean
 */
@Slf4j
@Component
public class EventListenerMethodsProcessor implements BeanDefinitionRegistryPostProcessor, ApplicationContextAware {

    @Resource
    private EventListenerProcessor processor;

    private ApplicationContext applicationContext;

    private BeanDefinitionRegistry registry;

    @Override
    public void postProcessBeanDefinitionRegistry(@NonNull BeanDefinitionRegistry registry) throws BeansException {
        this.registry = registry;
    }

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        String[] beanNames = beanFactory.getBeanDefinitionNames();

        for (String beanName : beanNames) {
            // 过滤掉 Scope 代理对象
            if (ScopedProxyUtils.isScopedTarget(beanName)) continue;

            // 获取 Bean 类型，处理可能出现的异常
            Class<?> beanType = beanFactory.getType(beanName);
            if (beanType == null) continue;

            // 1. 快速检查类上是否有相关注解的潜力 (Spring 工具类)
            if (!AnnotationUtils.isCandidateClass(beanType, Listener.class)) return;

            // 2. 筛选出包含 @Listener 注解的方法
            Map<Method, Listener> annotatedMethods = MethodIntrospector.selectMethods(
                    beanType,
                    (MethodIntrospector.MetadataLookup<Listener>) method
                            -> AnnotatedElementUtils.findMergedAnnotation(method, Listener.class));

            if (annotatedMethods.isEmpty()) return;

            if (log.isDebugEnabled()) {
                log.debug("Resolve candidate class {} bean named {} with any @Listener methods", beanType, beanName);
            }

            // 3. 为每个方法生成 EventListenerResolver
            annotatedMethods.forEach((method, listenerAnnotation) -> {
                // 获取 ApplyBinder 注解
                ApplyBinder applyBinder = AnnotatedElementUtils.findMergedAnnotation(method, ApplyBinder.class);

                // 创建 Supplier
                Supplier<EventListenerResolver> eventListenerResolverDescription =
                        () -> {
                            BinderManager binderManagerInstance = beanFactory.getBean(BinderManager.class);
                            return processor.process(
                                    beanName,
                                    method,
                                    listenerAnnotation,
                                    applyBinder,
                                    applicationContext,
                                    binderManagerInstance
                            );
                        };

                // 构建 BeanDefinition
                // BeanDefinition 是 Spring 容器里对一个 Bean 的“元数据描述”，相当于一份“如何创建和配置这个 Bean”的说明书
                // Spring 启动时会先把这些 BeanDefinition 收集起来，再按照定义创建真正的 Bean 实例。
                // 所以你看到的代码是先生成 BeanDefinition，后续再由 Spring 根据它实例化 EventListenerResolver
                BeanDefinition beanDefinition = BeanDefinitionBuilder.genericBeanDefinition(
                        EventListenerResolver.class,
                        eventListenerResolverDescription
                ).setPrimary(false).getBeanDefinition();

                // 生成 Bean 名称
                String beanDefinitionName = beanName + method.toGenericString() + "#GENERATED_LISTENER";

                if (log.isDebugEnabled())
                    log.debug("Generate event listener resolver bean definition {} named {}",
                            beanDefinition, beanDefinitionName);
                // 注册 BeanDefinition
                registry.registerBeanDefinition(beanDefinitionName, beanDefinition);
            });
        }
    }
}
