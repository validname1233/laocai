import yaml
from fastapi import FastAPI
import logging
from pydantic import BaseModel, Field
from typing import Optional
from services.get_access_token import get_access_token
from services.on_group_at_message_create import on_group_at_message_create
from services.verify_callback_url import verify_callback_url

# 设置日志格式和级别
logger = logging.getLogger("uvicorn")
logger.setLevel(logging.DEBUG)

app = FastAPI()


@app.get("/")
async def hello():
    logger.debug("he")
    return {"hello": "world"}


class Payload(BaseModel):
    id: Optional[str] = Field(default=None, description="payload id")
    op: int = Field(..., description="指的是 opcode，参考连接维护")
    d: dict = Field(..., description="代表事件内容，不同事件类型的事件内容格式都不同，请注意识别。主要用在op为 0 Dispatch 的时候")
    s: Optional[int] = Field(default=None, description="下行消息都会有一个序列号，标识消息的唯一性，客户端需要再发送心跳的时候，携带客户端收到的最新的s")
    t: Optional[str] = Field(default=None, description="代表事件类型。主要用在op为 0 Dispatch 的时候")


@app.post("/")
async def root(payload: Payload):
    with open("config.yaml", "r", encoding="utf-8") as f:
        config = yaml.safe_load(f)
    appid = config["appid"]
    bot_secret = config["secret"]
    base_url = "https://sandbox.api.sgroup.qq.com"

    logger.info(payload.model_dump())

    match payload.op:
        case 0:
            access_token = get_access_token(appid, bot_secret)

            match payload.t:
                case "GROUP_AT_MESSAGE_CREATE":
                    await on_group_at_message_create(base_url, access_token, payload.d)

        case 13:
            plain_token, signature_hex = verify_callback_url(bot_secret, payload.d)
            return {"plain_token": plain_token, "signature": signature_hex}

    return None
