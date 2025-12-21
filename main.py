import random  # 随机数模块：用于“是不是xxx”的随机回答
from fastapi import FastAPI  # Web 框架入口
from contextlib import asynccontextmanager  # 用于 FastAPI lifespan 生命周期管理
from loguru import logger  # 统一日志输出

# 以下为项目内模块导入
from config.logger import setup_logging  # 日志配置函数：初始化 loguru/uvicorn 的日志输出
from model.dto.event import Event  # 导入事件模型：NapCat/OneBot 回调的事件体
from model.dto.message import Message, MessageText, TextData, MessageReply, ReplyData  # 导入消息模型/消息段：用于解析与构造回复
from model.enums.event import EventType  # 导入事件类型枚举：用于判断是否为消息事件
from api.message import send_group_msg, send_private_msg  # 导入发送消息 API：群消息、私聊
from ai.service import ai_reponse  # 导入 AI 回复服务：处理 @ 机器人后的对话逻辑


@asynccontextmanager  # FastAPI 生命周期函数：启动/停止时执行
async def lifespan(app: FastAPI):  # 定义 lifespan：app 启动后进入、停止前退出
    # 在服务启动阶段初始化日志
    setup_logging()  # 初始化日志配置（控制台/文件/拦截 uvicorn 日志等）
    logger.info("QQ Robot 正在启动...")  # 输出启动日志
    yield  # 将控制权交给 FastAPI：服务运行阶段
    logger.info("QQ Robot 已停止")  # 输出停止日志


app = FastAPI(lifespan=lifespan)  # 创建 FastAPI 应用，并绑定生命周期钩子


@app.get("/")  # 健康检查/测试接口
async def root(): 
    return {"message": "Hello World"}  # 返回 JSON：用于确认服务可用


@app.post("/")  # NapCat/OneBot 回调入口
async def receive_event(event: Event):  # 接收事件体：由 FastAPI + Pydantic 自动解析
    logger.info("收到事件：{}", event)  # 记录收到的原始事件（结构化对象）
    if event.post_type == EventType.MESSAGE:  # 仅处理“消息事件”（忽略通知/请求等）
        message = Message.model_validate(event.model_dump())  # 将 Event 转成更具体的 Message（便于处理 message 段）
        if message.message_type == "group":  # 分支：群聊消息
            if message.is_at_self():  # 条件：是否 @ 了机器人自身
                logger.info(  # 输出日志：标记这是群@消息
                    "收到群聊的@消息, 群名{}, 用户{}",  # 日志模板：群名与发送者信息
                    message.group_name,  # 群名称
                    message.sender,  # 发送者信息（结构体）
                )
                ai_reponse(message)  # 调用 AI 服务：根据消息内容生成并发送回复
            elif message.text().strip().startswith("是不是"):  # 玩法分支：消息文本以“是不是”开头
                response_text = message.text().strip().split("是不是")[1].strip()  # 提取“是不是”后面的内容
                send_group_msg(  # 调用群消息发送接口（不 @，直接发到群）
                    message.group_id,  
                    [  # message 段列表：Reply + Text
                        MessageReply(data=ReplyData(id=str(message.message_id))),  # 回复引用：指向原消息
                        MessageText(  # 文本消息段
                            data=TextData(  # 文本数据
                                text="以牢财的意思，"  # 固定前缀
                                + ("是" if random.random() < 0.5 else "并不是")  # 随机“是/并不是”
                                + response_text  # 拼接用户提问内容
                            )
                        ),
                    ],
                )
        elif message.message_type == "private":  # 分支：私聊消息
            logger.info("收到私聊消息, 用户{}", message.sender)  # 记录私聊消息来源
            send_private_msg(  # 调用私聊发送接口
                message.sender.user_id,  # 目标用户 QQ
                [MessageText(data=TextData(text=" 冬至快乐喵"))],  # 发送文本消息段（固定回复）
            )

    return "OK"  # HTTP 响应：告诉回调方已成功处理


if __name__ == "__main__":  # 脚本入口：直接运行 main.py 时执行
    import uvicorn  # 导入 uvicorn：ASGI 服务器

    uvicorn.run(app, host="127.0.0.1", port=8000)  # 启动服务：监听本机 8000 端口
