# Event 从泛型信封 `Event<T>` 改为 sealed 继承体系

原实现里 `Event<T>` 是一个通用信封（`eventType`/`time`/`selfId`/`data: T`），`@Listener` 方法要靠反射掏出方法参数的 `ParameterizedType` 实际类型参数才能在运行时判断是否匹配——本质是用泛型模拟"事件子类型"，并为泛型擦除做变通。改造后 `Event` 是一个真正的 sealed 继承体系（`Event` → `MessageEvent` 中间层 → `GroupMessageEvent`/`FriendMessageEvent` 叶子类型），`@Listener` 方法按参数的声明类型直接匹配（支持声明为 `MessageEvent` 以同时接收两种叶子事件），不再需要反射掏泛型实参。

## Consequences

这是一次破坏性变更：所有 `@Listener` 方法签名从 `Event<IncomingGroupMessage>` 变为 `GroupMessageEvent`，唯一消费方 `laocai-app` 的调用点需要同步修改（见迁移策略 ADR）。未来新增事件类型（如 `bot_offline`）只需要给 sealed 接口新增一个 `permits` 分支，不再需要碰泛型匹配逻辑。
