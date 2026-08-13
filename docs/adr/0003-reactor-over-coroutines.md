# 继续使用 Reactor(Mono/Flux)，不引入 Kotlin 协程

框架的 Java 友好性是明确目标，而 Kotlin 协程的 `suspend fun` 对 Java 调用者天生不可见，需要额外维护一套阻塞/`CompletableFuture` 桥接层（simbot 就是这么做的）才能保持 Java 可用性。项目已经基于 Spring WebFlux，`Mono`/`Flux` 本身就是 Java 开发者可以直接调用（`.block()`、`.subscribe()`）的类型，因此选择保留 Reactor，`Bot` 上的发送/查询方法、事件流处理都继续用 `Mono`/`Flux` 表达。

## Considered Options

- 迁移到 Kotlin 协程，`@Listener` 支持 `suspend fun`，额外提供 Java 桥接层——更"Kotlin 原生"，但会显著增加维护成本，放弃。
- 保留 Reactor——被选中。
