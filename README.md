## 这是什么？（一句话）

这是一个 **NapCat / OneBot 风格事件的“接收端”**：用 **FastAPI** 启一个 HTTP 服务，接收上报事件（`POST /`），把事件按结构化模型解析后写入日志，方便你后续在这里接 **指令解析 / 自动回复 / AI 对话** 等功能。

目前代码的主要行为：

- **接收事件**：`POST /`（请求体会被解析为 `model.Event`）
- **打印日志**：输出完整事件 `model_dump()`，并额外打印 `plain_text()`（把消息段尽量还原成纯文本）
- **健康检查**：`GET /` 返回 `{"message":"Hello World"}`

---

## 适合谁用？

- 你已经有 **NapCat / OneBot** 的事件上报（HTTP 回调）
- 你想先把 **事件稳定收进来 + 打日志**，再逐步做机器人逻辑
- 你想要一个 **Pydantic 模型**来承接事件字段，避免“到处 dict 下标”

---

## 环境要求

- **Python**：`>= 3.13`（见 `pyproject.toml`）
- **依赖管理**：推荐用 **uv**（本项目带 `uv.lock`）

安装 uv（任选其一方式，按你的习惯来）：

- Windows（PowerShell）：

```bash
pip install uv
```

---

## 快速开始（跑起来就算成功）

在项目根目录执行：

```bash
uv run uvicorn main:app --host 0.0.0.0 --port 8000
```

看到类似日志就说明服务启动成功：

- `QQ Robot 正在启动...`
- Uvicorn 启动信息

然后访问健康检查：

- 浏览器打开 `http://127.0.0.1:8080/`
- 或 PowerShell：

```bash
curl http://127.0.0.1:8080/
```

---

## 如何测试 `POST /`（手动发一条“假事件”）

### PowerShell 一键测试

```bash
$body = @{
  post_type    = "message"
  message_type = "group"
  self_id      = 10001
  user_id      = 20002
  group_id     = 860323671
  message      = @(
    @{
      type = "text"
      data = @{ text = "你好，我是测试消息" }
    }
  )
  raw = @{}
} | ConvertTo-Json -Depth 10

Invoke-RestMethod -Method Post -Uri "http://127.0.0.1:8080/" -ContentType "application/json" -Body $body
```

### 预期效果

服务端日志会打印两条关键内容：

- **收到事件**：解析后的结构化内容（`event.model_dump()`）
- **plain_text=...**：把消息段尽量拼成纯文本（用于后续指令解析更方便）

接口返回值为：`OK`

---

## NapCat / OneBot 怎么对接？（把真实事件打进来）

你需要在 NapCat / OneBot 的配置里，把 **HTTP 上报地址** 指向这个服务：

- 本机调试：`http://127.0.0.1:8000/`
- 局域网/服务器：`http://<你的服务器IP>:8000/`

只要上报成功，你就能在 `logs/bot.log` 里看到每次事件的记录。

---

## 项目结构（你改代码一般只需要看这些）

```text
.
├─ main.py                 # FastAPI 入口：GET / + POST /
├─ model/
│  ├─ event.py             # 事件模型（NapCat/OneBot 风格）+ plain_text()
│  └─ __init__.py
├─ logger_config.py        # Loguru 日志配置（接管 uvicorn/fastapi 日志）
├─ logs/
│  └─ bot.log              # 日志文件（自动轮转/保留）
├─ config.toml             # 预留配置（当前代码未读取）
├─ config-local.toml       # 本地敏感配置示例（当前代码未读取，注意别提交真实 token）
└─ pyproject.toml          # 依赖与 Python 版本要求
```

---

## 事件模型说明（`model.Event`）

这个项目使用 `Pydantic` 把事件解析成强类型对象：

- **允许额外字段**：`extra="allow"`，所以就算 NapCat/OneBot 多给了字段也不会直接炸
- **核心字段**：`post_type` / `message_type` / `group_id` / `user_id` / `message`（消息段数组）
- **消息段**：形如 `{"type": "text"|"at"|..., "data": {...}}`
- **plain_text()**：把 `text` 和 `at` 尽量合并成一段纯文本（例如 `@12345你好`）

---

## 日志在哪里？怎么排查问题？

- **控制台日志**：默认 INFO
- **文件日志**：`logs/bot.log`（DEBUG，10MB 轮转，保留 7 天，异步写入不阻塞）

常见问题：

- **返回 422**：说明请求体不符合 `model.Event` 的最基本结构（最常见是 `message` 不是数组，或数组里的段缺 `type`）
- **收不到事件**：先确认 NapCat/OneBot 上报地址是否能访问到你的 8080 端口（本机/内网/防火墙）

---

## 下一步怎么扩展？（你大概率会这么做）

建议的开发路线：

- 在 `receive_event()` 里根据 `event.is_group()/is_private()`、`event.plain_text()` 做简单指令解析
- 需要“发消息”时，再接入 NapCat/OneBot 的 HTTP API（目前仓库里还没实现这部分）。

