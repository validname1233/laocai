# 引入显式的 `Bot` 对象，并把 `BotSender` 的能力并入其中

原实现把主动调用能力放在 `BotSender` 上，消费方需要认识一个只表达“发送器”的类型。改造后引入 `Bot`，承载发送/查询能力（`sendGroupMsg`、`sendPrivateMsg`、`sendGroupAnnouncement`、`getUserProfile`），不再单独暴露 `BotSender`。

`Bot` 当前只封装 Milky WebClient 和主动调用能力，不持有 `selfId`。机器人身份仍由每条入站 `Event` 提供。未来如果真正支持多账号，需要先明确身份的配置或发现来源，再扩展 `Bot`，而不是让文档提前承诺实现中不存在的状态。

## Considered Options

- 保留 `BotSender`——名称只表达发送，无法覆盖查询能力，放弃。
- 让 `Bot` 同时持有身份与能力——目前没有稳定的 `selfId` 初始化来源，会制造虚假的领域语义，暂不采用。
- 让 `Bot` 作为主动能力入口，身份留在 `Event`——被选中。

## Consequences

消费方（`laocai-app`）里所有注入 `BotSender` 的地方改为注入 `Bot`。现阶段仍是单账号 Spring Bean，但该限制来自自动配置只创建一个 `Bot`，不是 `Bot` 已经建模了账号身份。
