# 只做单协议(Milky)的 QQ 机器人框架，不照搬 simbot 的多协议组件架构

这次改造以 simple-robot(simbot) 为参照做 Kotlin 化，但 simbot 的核心是多协议、组件可插拔的通用框架（`simbot-api` 与各 `simbot-component-*` 分离）。我们目前只有 Milky 一种协议实现，也没有第二个协议的现实需求，因此不引入 component/plugin 抽象层，只在语言层面（Kotlin + Java 友好 API）和局部设计手法上借鉴 simbot。

## Considered Options

- 完全对标 simbot，做协议无关的 core + 可插拔 component 实现——为不存在的扩展需求提前买单，放弃。
- 只做语言/API 层面的 Kotlin 化，协议逻辑与框架核心不拆分——被选中。

## Consequences

框架里目前不会出现"Component"/"Plugin"这类抽象；如果未来真的要接入第二个协议，需要在那时重新评估是否要引入这层抽象，而不是现在预留。
