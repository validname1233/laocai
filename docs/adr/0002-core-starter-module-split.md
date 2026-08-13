# 拆分为 laocai-bot-core（无 Spring 依赖）与 laocai-bot-spring-boot-starter（薄集成层）

原来的单模块把 Spring 相关代码（`@EnableConfigurationProperties`、`ApplicationRunner`、从 `ApplicationContext` 反射取 bean）和领域/协议代码（`Event`、`Segment`、`Bot`、`Dispatcher`）混在一起。拆成两个模块后，`laocai-bot-core` 是纯 Kotlin、不依赖 Spring 的运行时，`laocai-bot-spring-boot-starter` 只负责把 core 接到 Spring 容器（自动配置、`@ConfigurationProperties`、Bean 装配）。

## Consequences

core 可以脱离 Spring 单独测试和复用；多了一个 Gradle 模块的构建成本，两个模块都需要各自的 `build.gradle.kts`。
