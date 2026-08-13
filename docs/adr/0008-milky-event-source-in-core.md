# 入站事件流（MilkyEventSource）搬进 core，明确 core/starter 边界

ADR 0002 把模块拆成 core（不依赖 Spring 的协议/领域运行时）与 starter（把 core 接到 Spring 容器），说的是"协议代码归 core"。但拆分时只把 `Bot` 的出站协议逻辑（发消息、查资料）放进了 core，SSE 事件流的入站协议逻辑——连接 `/event`、反序列化、背压策略、并发分发、断线重试——留在了 starter 的 `LaocaiBotRunner` 里，跟 Spring 的 `ApplicationRunner` 生命周期耦在一起。这是对 ADR 0002 边界的一次遗漏：同样是"连 Milky"，出站在 core、入站却在 starter，两侧不对称，而且这段本来可以脱离 Spring 独立测试的逻辑，实际上一直没有测试覆盖。

## Decision

把入站协议逻辑拆成两层，分别归位：

- **`MilkyEventSource`（core）**：只负责连接 `/event`、反序列化、断线无限重试，对外是一条不会以 `onError`/`onComplete` 终止的 `Flux<Event>`。不关心下游怎么消费。
- **`EventDispatcher.consume`（core）**：接收任意 `Flux<Event>`，套上背压缓冲（超限丢最新）和并发分发，调用已有的 `dispatch`。消费策略（要不要限流、并发多大）跟事件源本身无关，所以放在分发器这一侧而不是事件源那一侧。

`laocai-bot-spring-boot-starter` 的 `LaocaiBotRunner` 因此退化成纯 Spring 装配胶水：注册 `EventListenerResolver`，再把 `LaocaiBotConfigurationProperties` 里读到的并发度/缓冲区大小传给 `eventDispatcher.consume(milkyEventSource.eventFlux(), ...)`，本身不包含任何协议或消费策略逻辑。

重试延迟（固定 5 秒）继续硬编码在 `MilkyEventSource` 内部，不开放成配置项——目前没有场景需要调整它，不为假设的需求加旋钮。

## Consequences

现在 core/starter 的边界可以精确描述为：**core 拥有全部 Milky 协议逻辑（出站 `Bot` + 入站 `MilkyEventSource`）和事件消费策略（`EventDispatcher.consume`）；starter 只做 Spring bean 装配和配置绑定，不包含任何协议或策略代码。** 以后如果又有人想往 `LaocaiBotRunner` 里加协议相关逻辑，这条边界能被直接引用来指出问题。

`EventDispatcher.consume` 是新组合出来的逻辑，补了单元测试（用合成的 `Flux<Event>`，不需要真实 HTTP）；`MilkyEventSource` 本身的连接/重试逻辑是从原 `LaocaiBotRunner` 原样平移，没有新增测试覆盖——测试它需要 mock HTTP server（如 MockWebServer/WireMock），现有项目没有这个依赖，为一次平移引入新测试基建不划算，留给以后真的需要时再补。
