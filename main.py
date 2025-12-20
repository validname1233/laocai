from fastapi import FastAPI
from contextlib import asynccontextmanager
from loguru import logger
# 导入上面的配置函数
from logger_config import setup_logging
from model.dto.event import Event
from model.dto.message import Message
from model.enums.event import EventType

@asynccontextmanager
async def lifespan(app: FastAPI):
    # 启动时配置日志
    setup_logging()
    logger.info("QQ Robot 正在启动...")
    yield
    logger.info("QQ Robot 已停止")

app = FastAPI(lifespan=lifespan)

@app.get("/")
async def root():
    return {"message": "Hello World"}

@app.post("/")
async def receive_event(event: Event):
    # FastAPI 会自动把请求体解析为 Event（解析失败会返回 422）
    logger.info("收到事件：{}", event.model_dump())
    if event.post_type == EventType.MESSAGE:
        message = Message.model_validate(event.model_dump())
        logger.info("收到消息：{}", message.message[0])
    return "OK"

if __name__ == "__main__":
    import uvicorn

    uvicorn.run(app, host="0.0.0.0", port=8000)
