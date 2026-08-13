# 引入显式的 `Bot` 领域对象，并把 `BotSender` 的能力并入其中

原实现是"全局单例式"的：一个 `milkyWebClient`、一个 `BotSender`、一个 `LaocaiBotRunner`，没有一个类型代表"一个机器人账号"本身，身份信息（`selfId`）只散落在 `Event` 里。改造后引入 `Bot` 作为核心域对象，承载身份与发送/查询能力（`sendGroupMsg`/`sendPrivateMsg`/`sendGroupAnnouncement`/`getUserProfile`，即原 `BotSender` 的方法），不再单独暴露 `BotSender` 类型。目前仍只支持单账号（一个 Spring 应用只有一个 `Bot` bean），但把身份概念收敛到一个类型上，为以后如果要支持多账号留出扩展点，而不需要现在就实现多账号管理。

## Considered Options

- 保持 `Bot`（身份）与 `BotSender`（能力）分离——更接近 simbot 的职责划分，但对当前唯一场景（单账号、能力和身份从不分开使用）只增加了一次跳转成本，放弃。
- `Bot` 直接持有全部能力——被选中。

## Consequences

消费方（`laocai-app`）里所有注入 `BotSender` 的地方改为注入 `Bot`，方法调用方式不变。
