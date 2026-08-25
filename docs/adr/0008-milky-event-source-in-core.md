# 入站事件流与 Spring Boot 生命周期分层

最初的实现把连接 `/event`、反序列化、背压、并发分发和断线重试全部放在 `LaocaiBotRunner` 中，使 Milky 协议行为与 Spring Boot 的 `ApplicationRunner` 生命周期混在一起。模块已经合并为单一 starter，但这两个职责仍有不同的变化原因，不能因为物理同模块而重新耦合。

## Decision

把入站事件逻辑分成三层：

- **`MilkyEventSource`**：只负责连接 `/event`、把 SSE 数据反序列化为 `Event`、断线无限重试，对外提供 `Flux<Event>`，不关心下游如何消费。
- **`EventDispatcher.consume`**：接收任意 `Flux<Event>`，应用有界背压缓冲（超限丢最新）和并发分发，再调用已有的 `dispatch`。
- **`LaocaiBotRunner`**：只属于 Spring Boot 集成层，注册容器扫描生成的 `EventListenerResolver`，再把配置中的并发度和缓冲区大小传给 `EventDispatcher.consume`。

重试延迟固定为 5 秒，不开放配置项。dispatcher 默认并发度为 32、缓冲上限为 5000，溢出策略保持 `DROP_LATEST`。

## Consequences

统一 Artifact 内部仍有明确边界：Milky 协议与事件消费策略不依赖 `ApplicationContext` 或 Bean 生命周期，自动配置与 Runner 只负责容器装配和启动时机。以后不应把 HTTP/SSE 协议、重连或背压策略重新写入 Runner。

`EventDispatcher.consume` 使用合成 `Flux<Event>` 做单元测试；`MilkyEventSource` 的真实 HTTP/SSE 连接与重试仍未引入 MockWebServer/WireMock 覆盖，保留为独立测试任务。
