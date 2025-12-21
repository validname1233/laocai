### 项目简介

这是一个基于 **FastAPI** 的 NapCat/OneBot 回调服务，用来接收 QQ 消息事件并自动回复。

- **群聊 @ 机器人**：走 AI 对话回复（带上下文）
- **群聊“是不是xxx”**：随机回复“是/并不是”，并用 `reply` 引用原消息
- **私聊消息**：固定回复

---

### 快速开始

#### 1) 安装依赖

项目使用 `uv` 管理依赖（也兼容普通 venv + pip）。

```bash
uv sync
```

#### 2) 配置文件

配置分两层加载：**`config.toml`（默认） + `config-local.toml`（本地覆盖，已被 `.gitignore` 忽略）**。

- **`config.toml`**（需要提交到仓库）
  - **`[bot-api].base_url`**：NapCat HTTP API 地址（例如 `http://127.0.0.1:3000`）
  - **`[ai].base_url`**：OpenAI 兼容 API 的 base_url
  - **`[ai].model`**：模型名
- **`config-local.toml`**（不要提交）
  - **`[bot-api].token`**：NapCat token
  - **`[ai].api_key`**：AI API Key

#### 3) 启动服务

```bash
uv run uvicorn main:app --host 127.0.0.1 --port 8000
```

---

### 机器人行为说明

#### 群聊

- **@ 机器人**：调用 `ai/service.py` 的 `ai_reponse()` 进行 AI 回复
  - **指令：`开大脚`**：清空该用户上下文并重新注入 `PREAMBLE_MESSAGES`（相当于“重置对话”）
- **文本以“是不是”开头**：随机回复“是/并不是”，并引用原消息

#### 私聊

- 当前默认固定回复（可按需扩展）。

---

### 目录结构

- **`main.py`**：FastAPI 回调入口与路由逻辑
- **`model/`**：NapCat/OneBot 事件与消息段的 Pydantic 模型
  - `model/dto/message.py`：`MessageData` 判别联合（按 `type` 解析 text/at/image…）
  - `model/enums/message.py`：消息枚举（修复 `PicSubType`）
- **`api/`**
  - `api/message.py`：封装 NapCat 发送接口（群消息/群@/私聊），并负责 `MessageData` 序列化
- **`ai/`**
  - `ai/agent.py`：创建 LangChain Agent（含 `SYSTEM_PROMPT` / `PREAMBLE_MESSAGES` / 截断策略）
  - `ai/service.py`：把群@消息接入 agent，并发送回复
- **`config/`**
  - `config/toml.py`：合并加载 `config.toml` + `config-local.toml`
  - `config/logger.py`：Loguru 日志配置（控制台 INFO + 文件 DEBUG）
- **`logs/`**：运行日志目录（已在 `.gitignore` 忽略）

---

### 本次改动摘要（相对历史提交）

- **日志**：统一使用 Loguru，新增 `config/logger.py`，并把 `logs/` 加入 `.gitignore`
- **配置**：新增 `config/toml.py`，支持 `config.toml` + `config-local.toml` 合并加载
- **消息模型**：`MessageData` 改为按 `type` 的判别联合，解决图片等消息段解析时报大量校验错误的问题
- **发送接口**：新增 `api/message.py`，修复 `list` 不能 `model_dump()` 的序列化问题，并把 `@` 插到消息最前面
- **AI 对话**：新增 `ai/agent.py` / `ai/service.py`，支持群聊 @ 触发 AI 回复与“开大脚”重置
