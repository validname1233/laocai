from __future__ import annotations

from typing import Any

from pydantic import BaseModel, ConfigDict, Field


class Sender(BaseModel):
    """消息发送者信息（字段可能随协议变化，允许额外字段）。"""

    model_config = ConfigDict(extra="allow")

    user_id: int | None = None
    nickname: str | None = None
    card: str | None = None
    role: str | None = None  # group 场景可能有：member/admin/owner 等


class MessageSegmentData(BaseModel):
    """消息段 data（最常用是 text / at）。"""

    model_config = ConfigDict(extra="allow")

    text: str | None = None
    qq: int | str | None = None


class MessageSegment(BaseModel):
    """消息段：{'type': 'text'|'at'|..., 'data': {...}}"""

    model_config = ConfigDict(extra="allow")

    type: str
    data: MessageSegmentData = Field(default_factory=MessageSegmentData)


class Event(BaseModel):
    """
    NapCat / OneBot 风格事件体（按你贴的私聊/群聊样例建模）。
    注意：为兼容 raw 等超大/不稳定字段，开启 extra=allow。
    """

    model_config = ConfigDict(extra="allow")

    # 基础字段
    self_id: int | None = None
    user_id: int | None = None
    time: int | None = None

    post_type: str | None = None  # message / notice / request / meta_event ...
    message_type: str | None = None  # private / group
    sub_type: str | None = None

    # 消息标识
    message_id: int | None = None
    message_seq: int | None = None
    real_id: int | None = None
    real_seq: str | int | None = None

    # 群/私聊相关
    group_id: int | None = None
    group_name: str | None = None
    target_id: int | None = None

    sender: Sender | None = None

    raw_message: str | None = None
    font: int | None = None

    message: list[MessageSegment] = Field(default_factory=list)
    message_format: str | None = None  # array / string

    # raw 字段非常大且结构不稳定，直接放 dict
    raw: dict[str, Any] = Field(default_factory=dict)

    def plain_text(self) -> str:
        """把 message 段尽量还原成纯文本（用于日志/简单指令解析）。"""
        parts: list[str] = []
        for seg in self.message:
            if seg.type == "text" and seg.data.text is not None:
                parts.append(seg.data.text)
            elif seg.type == "at" and seg.data.qq is not None:
                parts.append(f"@{seg.data.qq}")
        return "".join(parts).strip()

    def is_private(self) -> bool:
        return self.post_type == "message" and self.message_type == "private"

    def is_group(self) -> bool:
        return self.post_type == "message" and self.message_type == "group"

