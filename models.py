from pydantic import BaseModel, Field
from typing import Optional


class Payload(BaseModel):
    """WebHook负载模型"""
    id: Optional[str] = Field(default=None, description="payload id")
    op: int = Field(..., description="指的是 opcode，参考连接维护")
    d: dict = Field(..., description="代表事件内容，不同事件类型的事件内容格式都不同，请注意识别。主要用在op为 0 Dispatch 的时候")
    s: Optional[int] = Field(default=None, description="下行消息都会有一个序列号，标识消息的唯一性，客户端需要再发送心跳的时候，携带客户端收到的最新的s")
    t: Optional[str] = Field(default=None, description="代表事件类型。主要用在op为 0 Dispatch 的时候")


class VerifyEvent(BaseModel):
    plain_token: str
    event_ts: str


class GroupAtMessage(BaseModel):
    """群组@消息创建事件模型"""
    id: str
    content: str
    timestamp: str
    author: object
    group_openid: str
    attachments: Optional[list[object]] = Field(default=None, description="消息附件")
    group_id: str
    message_scene: object
    message_type: int


class GroupAtMessageResponse(BaseModel):
    """消息响应模型"""
    content: str
    msg_id: str
    msg_type: int = 0 