# 合并为单一 laocai-bot-spring-boot-starter，并保留逻辑分层

`laocai-bot-core` 与 `laocai-bot-spring-boot-starter` 的物理拆分没有形成真正的框架无关核心：core 已直接使用 Spring WebFlux、Spring Core、Reactor 和 Jackson，只是不依赖 `ApplicationContext` 与 Spring Boot 生命周期。同时仓库内所有消费方都只依赖 starter，独立 core Artifact 没有实际使用场景，却增加了重复 Gradle 配置、SNAPSHOT 坐标解析、版本同步和双 Artifact 发布成本。

## Decision

只保留 `indi.kyson:laocai-bot-spring-boot-starter` 一个 Artifact，删除 `laocai-bot-core` build 和坐标。原 core 源码并入 starter，并从包路径中删除 `.core` 层级；开发期直接迁移，不提供兼容 Artifact、旧包 typealias 或弃用转发。

物理模块合并不代表职责混合。统一模块内部继续保持两层：

- **协议与事件运行时**：`Bot`、`MilkyEventSource`、事件/Segment 模型、监听器匹配与 `EventDispatcher`。允许使用 WebClient、Spring Core 工具、Reactor、Jackson 和 SLF4J，但不访问 `ApplicationContext`，不负责 Bean 创建或 Spring Boot 生命周期。
- **Spring Boot 集成**：配置绑定、自动配置、BeanDefinition 扫描和应用启动后的事件流订阅。

Spring Boot 是唯一正式支持的完整集成方式。`Bot` 仍可直接用 `WebClient` 构造，方便测试和高级调用，但不再承诺独立的非 Spring 启动、监听器扫描或生命周期方案。

starter 使用标准 Boot 自动配置：存在 `laocai.milky.url` 时启用，不再需要 `@EnableLaocaiBot`。`access-token` 可选；为空时不发送 Authorization 请求头。公开的运行时 Bean 在用户提供同类型实例时退让。

## Consequences

- 框架只维护一个版本、一个 publication 和一套构建配置。
- `laocai-app` 只依赖 starter，根 composite build 不再解析 core SNAPSHOT。
- 包名与 Artifact 坐标发生直接破坏性迁移，但项目仍处于开发期且没有外部兼容承诺。
- 物理模块不再强制依赖方向，因此通过包职责、内部可见性、测试和本 ADR 约束容器生命周期代码不得渗入协议/事件运行时。
