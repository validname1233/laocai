# 一次性重写，不为旧 API 保留兼容层

这次改造涉及语言（Java→Kotlin）、模块拆分、`Event` 模型（泛型信封→sealed 继承体系）、`Bot`/`BotSender` 合并等多处破坏性变更。唯一真实消费方 `laocai-app` 只有约 5 个调用点（`TexasHandler`/`AiHandler`/`TestHandler`/`BotSenderTool` 及测试），且是同一人维护的私有单体仓库，没有外部使用者需要兼容期。因此选择一次性完成框架重写并同步更新这些调用点，不设计新旧 API 兼容适配层。

## Considered Options

- 保留兼容层，框架和业务迁移分两次变更——兼容层本身除了过渡期没有长期价值，且当前调用点数量很少，分两次反而增加总工作量，放弃。
- 一次性重写——被选中。
