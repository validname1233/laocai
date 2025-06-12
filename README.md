# 老蔡QQ机器人 🐱

基于FastAPI的智能QQ群机器人，支持AI聊天和掷骰功能。

## ✨ 功能特性

- 🤖 **AI聊天**: 基于猫娘人设的智能对话，支持上下文记忆
- 🎲 **掷骰功能**: 支持多种掷骰命令，如 `r3d6`
- 📝 **日志管理**: 完善的日志记录和级别控制
- ⚡ **高性能**: 基于FastAPI框架，支持异步处理
- 🔧 **易于配置**: YAML配置文件，支持环境切换

## 🏗️ 项目结构

```
laocai-python/
├── app/                        # 主应用包
│   ├── __init__.py
│   ├── main.py                 # FastAPI应用入口
│   ├── config.py               # 配置管理
│   ├── logging_config.py       # 日志配置
│   ├── models.py               # 数据模型
│   ├── api/                    # API路由
│   │   ├── __init__.py
│   │   └── routes.py           # 路由定义
│   └── services/               # 业务服务
│       ├── __init__.py
│       ├── auth_service.py     # 认证服务
│       ├── message_service.py  # 消息处理服务
│       ├── neko_service.py     # AI聊天服务
│       └── verification_service.py  # 验证服务
├── services/                   # 原有服务（待迁移）
├── main.py                     # 原主文件（已废弃）
├── main_new.py                 # 新主入口文件
├── config.yaml.example         # 配置文件模板
├── config.yaml                 # 实际配置文件（需自行创建）
├── pyproject.toml              # 项目依赖配置
└── README.md                   # 项目说明
```

## 🚀 快速开始

### 1. 安装依赖

```bash
# 使用 uv 安装依赖（推荐）
uv sync

# 或使用 pip
pip install -r requirements.txt
```

### 2. 配置文件

```bash
# 复制配置模板
cp config.yaml.example config.yaml

# 编辑配置文件，填入您的实际配置
```

配置文件说明：
- `appid`: QQ机器人的AppID
- `secret`: QQ机器人的Secret
- `neko_key`: SiliconFlow API密钥
- `base_url`: QQ API基础URL
- `log_level`: 日志级别

### 3. 启动服务

```bash
# 开发模式启动
uvicorn main_new:app --host 0.0.0.0 --port 8000 --reload

# 或直接运行
python main_new.py
```

### 4. WebHook配置

在QQ机器人管理后台配置WebHook地址：
```
http://your-domain.com:8000/webhook
```

## 🔧 项目优化亮点

### 1. **模块化架构**
- 按功能模块划分代码结构
- 清晰的依赖关系和接口定义
- 便于测试和维护

### 2. **配置管理优化**
- 统一的配置管理类
- 支持环境变量和文件配置
- 配置验证和错误处理

### 3. **日志系统改进**
- 统一的日志配置
- 支持多种日志级别
- 结构化日志输出

### 4. **错误处理增强**
- 全面的异常捕获和处理
- 用户友好的错误提示
- 详细的错误日志记录

### 5. **代码质量提升**
- 类型提示和文档字符串
- 符合PEP8的代码风格
- 可复用的服务类

## 📚 API文档

启动服务后，可以通过以下地址查看API文档：
- Swagger UI: http://localhost:8000/docs
- ReDoc: http://localhost:8000/redoc

## 🎯 使用示例

### AI聊天
在群内@机器人并发送：
```
@机器人 /说话 你好，你是谁？
```

### 掷骰功能
```
@机器人 r3d6    # 掷3个6面骰子
@机器人 r1d100  # 掷1个100面骰子
```

## 🔄 从旧版本迁移

如果您正在使用旧版本的机器人，请按以下步骤迁移：

1. 备份您的 `config.yaml` 文件
2. 使用新的启动方式: `python main_new.py`
3. 旧版本文件将逐步被新版本替代

## 🤝 贡献指南

1. Fork 本项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情

## 🙏 致谢

- [FastAPI](https://fastapi.tiangolo.com/) - 现代、快速的Web框架
- [SiliconFlow](https://siliconflow.cn/) - AI服务提供商
- [QQ机器人开放平台](https://bot.q.qq.com/) - 官方机器人平台

---

如有问题或建议，欢迎提交 Issue 或 Pull Request！