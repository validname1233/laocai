# Context Map

本仓库包含两个边界清晰、存在依赖关系的领域上下文。

## Bot 框架上下文

- **领域文档**：`CONTEXT.md`
- **主要范围**：`laocai-bot-spring-boot-starter`、框架核心代码以及与 Milky/LLBot 通信和事件分发相关的代码
- **职责**：连接 LLBot，处理 Milky 协议，解析 `Event`，提供 `Bot`、`Listener`、`Filter` 和事件分发能力
- **边界**：不包含 AI 对话、德州扑克等消费方业务逻辑
- **架构决策**：根目录 `docs/adr/` 中与框架相关的 ADR

根目录 `CONTEXT.md` 是 Bot 框架上下文的领域词汇表。`bin/CONTEXT.md` 与其内容相同，不单独构成新的领域上下文。

## AI 主动对话上下文

- **领域文档**：`laocai-app/CONTEXT.md`
- **主要范围**：`laocai-app`
- **边界**：使用 Bot 框架提供的事件和发送能力，但不重新定义框架的 `Event`、`Listener`、`Bot` 等术语
- **架构决策**：根目录 `docs/adr/` 中与应用相关的 ADR；如以后增加 `laocai-app/docs/adr/`，同时读取其中相关决策

## 上下文关系

```text
应用上下文（laocai-app）
              │
              │ 使用框架事件与发送能力
              ▼
Bot 框架上下文（laocai-bot-spring-boot-starter）
              │
              ▼
        Milky / LLBot
```

修改 Bot 框架时，优先使用 Bot 框架上下文；修改 `laocai-app` 时，同时读取 Bot 框架上下文和应用上下文。
