from __future__ import annotations

from pydantic import BaseModel, ConfigDict, Field

from model.enums.event import EventType


class Event(BaseModel):
    """
    NapCat / OneBot 风格事件体（按你贴的私聊/群聊样例建模）。
    注意：为兼容 raw 等超大/不稳定字段，开启 extra=allow。
    """

    model_config = ConfigDict(extra="allow")

    # 基础字段
    time: int = Field(description="事件时间戳")
    post_type: EventType = Field(description="事件类型")
    self_id: int = Field(description="收到事件的机器人 QQ 号")