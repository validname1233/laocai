package indi.kyson.laocai.bot.listener

import indi.kyson.laocai.bot.core.annotation.Listener
import indi.kyson.laocai.bot.core.listener.EventListenerProcessor
import indi.kyson.laocai.bot.core.listener.EventListenerResolver
import org.slf4j.LoggerFactory
import org.springframework.aop.scope.ScopedProxyUtils
import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.beans.factory.support.BeanDefinitionBuilder
import org.springframework.beans.factory.support.BeanDefinitionRegistry
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.core.MethodIntrospector
import org.springframework.core.annotation.AnnotatedElementUtils
import org.springframework.core.annotation.AnnotationUtils
import java.util.function.Supplier

/**
 * 扫描并注册事件监听器解析器。
 *
 * 先在 BeanFactory 后处理阶段收集元数据，再延迟实例化监听器，可以避免启动期提前创建不必要的对象。
 */
class EventListenerResolverRegistryProcessor : BeanDefinitionRegistryPostProcessor, ApplicationContextAware {

    private val logger = LoggerFactory.getLogger(EventListenerResolverRegistryProcessor::class.java)

    private lateinit var applicationContext: ApplicationContext

    private lateinit var registry: BeanDefinitionRegistry

    override fun postProcessBeanDefinitionRegistry(registry: BeanDefinitionRegistry) {
        this.registry = registry
    }

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        this.applicationContext = applicationContext
    }

    override fun postProcessBeanFactory(beanFactory: ConfigurableListableBeanFactory) {
        val processor = beanFactory.getBean(EventListenerProcessor::class.java)
        val beanNames = beanFactory.beanDefinitionNames

        for (beanName in beanNames) {
            // 过滤掉 Scope 代理对象
            if (ScopedProxyUtils.isScopedTarget(beanName)) continue

            // 获取 Bean 类型，处理可能出现的异常
            val beanType = beanFactory.getType(beanName) ?: continue

            // 1. 快速检查类上是否有相关注解的潜力 (Spring 工具类)
            if (!AnnotationUtils.isCandidateClass(beanType, Listener::class.java)) continue

            // 2. 筛选出包含 @Listener 注解的方法
            val annotatedMethods = MethodIntrospector.selectMethods(
                beanType,
                MethodIntrospector.MetadataLookup<Listener> { method ->
                    AnnotatedElementUtils.findMergedAnnotation(method, Listener::class.java)
                },
            )

            if (annotatedMethods.isEmpty()) continue

            logger.debug("Resolve candidate class {} instance named {} with any @Listener methods", beanType, beanName)

            // 3. 为每个方法生成 EventListenerResolver
            annotatedMethods.forEach { (method, _) ->

                // 创建 Supplier
                val eventListenerResolverDescription = Supplier<EventListenerResolver> {
                    processor.process(beanName, method) { name -> applicationContext.getBean(name) }
                }

                // 构建 BeanDefinition
                // BeanDefinition 是 Spring 容器里对一个 Bean 的“元数据描述”，相当于一份“如何创建和配置这个 Bean”的说明书
                // Spring 启动时会先把这些 BeanDefinition 收集起来，再按照定义创建真正的 Bean 实例。
                // 所以你看到的代码是先生成 BeanDefinition，后续再由 Spring 根据它实例化 EventListenerResolver
                val beanDefinition: BeanDefinition = BeanDefinitionBuilder.genericBeanDefinition(
                    EventListenerResolver::class.java,
                    eventListenerResolverDescription,
                ).setPrimary(false).beanDefinition

                // 生成 Bean 名称
                val beanDefinitionName = "$beanName${method.toGenericString()}#GENERATED_LISTENER"

                if (logger.isDebugEnabled) {
                    logger.debug(
                        "Generate event listener resolver instance definition {} named {}",
                        beanDefinition,
                        beanDefinitionName,
                    )
                }
                // 注册 BeanDefinition
                registry.registerBeanDefinition(beanDefinitionName, beanDefinition)
            }
        }
    }
}
