from __future__ import annotations

from typing import Annotated, Any, Literal, Optional, Union

from pydantic import BaseModel, ConfigDict, Field

from model.enums.message import MessageDataType, PicSubType
from model.enums.data import UserSex, GroupMemberRole

from model.dto.event import Event

class Message(Event):
    message_type: Literal["private", "group"] = Field(description="消息类型：私聊/群聊")
    sub_type: Literal["friend", "group", "normal"] = Field(description="子类型：好友/群聊/正常")
    message_id: int = Field(description="消息ID")
    user_id: int = Field(description="发送者QQ号")
    message: list[MessageData] = Field(description="消息段列表")
    raw_message: str = Field(description="原始消息内容")
    font: int = Field(description="字体")
    sender: Sender = Field(description="发送者信息")
    message_seq: int = Field(description="和message_id一样")
    real_id: int = Field(description="和message_id一样")
    real_seq: str = Field(description="自行扩展")
    message_format: Literal["array", "string"] = Field(description="消息格式")
    # raw 字段非常大且结构不稳定，直接放 dict
    raw: dict[str, Any] = Field(default_factory=dict)

    group_id: Optional[int] = Field(default=None, description="群ID（可选）")
    group_name: Optional[str] = Field(default=None, description="群名称（可选）")
    target_id: Optional[int] = Field(default=None, description="临时会话目标 QQ 号（可选）")
    temp_source: Optional[int] = Field(default=None, description="临时会话来源（可选）")

    def is_at_self(self) -> bool:
        return any(msg.type == MessageDataType.AT and msg.data.qq == str(self.self_id) for msg in self.message)

    def text(self) -> str:
        return "".join(msg.data.text for msg in self.message if msg.type == MessageDataType.TEXT)


class MessageText(BaseModel):
    type: Literal[MessageDataType.TEXT] = Field(default=MessageDataType.TEXT,description="消息类型")
    data: TextData = Field(description="文本数据")

class TextData(BaseModel):
    text: str = Field(description="文本内容")

class MessageFace(BaseModel):
    type: Literal[MessageDataType.FACE] = Field(default=MessageDataType.FACE,description="消息类型")
    data: FaceData = Field(description="表情数据")

class FaceData(BaseModel):
    id: str = Field(description="QQ 表情 ID")
    result_id: Optional[str] = Field(default=None, alias="resultId", description="结果 ID")
    chain_count: Optional[int] = Field(default=None, alias="chainCount", description="链式计数")

class MessageMFace(BaseModel):
    type: Literal[MessageDataType.MFACE] = Field(default=MessageDataType.MFACE,description="消息类型")
    data: MFaceData = Field(description="商城表情数据")

class MFaceData(BaseModel):
    emoji_package_id: int = Field(description="表情包ID")
    emoji_id: int = Field(description="表情ID")
    key: str = Field(description="表情键")
    summary: str = Field(description="表情摘要")

class MessageAt(BaseModel):
    type: Literal[MessageDataType.AT] = Field(default=MessageDataType.AT,description="消息类型")
    data: AtData = Field(description="@数据")

class AtData(BaseModel):
    qq: str = Field(description="被@的QQ号")
    name: Optional[str] = Field(default=None, description="被@的昵称")

class MessageReply(BaseModel):
    type: Literal[MessageDataType.REPLY] = Field(default=MessageDataType.REPLY,description="消息类型")
    data: ReplyData = Field(description="回复数据")

class ReplyData(BaseModel):
    id: str = Field(description="回复消息ID")

class MessageFileBase(BaseModel):
    data: FileBaseData = Field(description="文件基础数据")

class FileBaseData(BaseModel):
    # NapCat/OneBot 的 data 字段经常会多出 file_size 等扩展字段；放开 extra
    model_config = ConfigDict(extra="allow")

    path: Optional[str] = Field(default=None, description="文件路径")
    thumb: Optional[str] = Field(default=None, description="缩略图路径")
    name: Optional[str] = Field(default=None, description="文件名")
    file: str = Field(description="文件ID")
    url: Optional[str] = Field(default=None, description="文件URL")
    file_size: Optional[int | str] = Field(default=None, description="文件大小（可选，NapCat 扩展）")

class MessageImage(MessageFileBase):
    type: Literal[MessageDataType.IMAGE] = Field(default=MessageDataType.IMAGE,description="消息类型")
    data: ImageData = Field(description="图片数据")

class ImageData(FileBaseData):
    summary: Optional[str] = Field(default=None, description="图片摘要")
    sub_type: Optional[PicSubType] = Field(default=None, description="图片子类型")

class MessageRecord(MessageFileBase):
    type: Literal[MessageDataType.VOICE] = Field(default=MessageDataType.VOICE,description="消息类型")

class MessageFile(MessageFileBase):
    type: Literal[MessageDataType.FILE] = Field(default=MessageDataType.FILE,description="消息类型")

class MessageVideo(MessageFileBase):
    type: Literal[MessageDataType.VIDEO] = Field(default=MessageDataType.VIDEO,description="消息类型")

class MessageNode(BaseModel):
    type: Literal[MessageDataType.NODE] = Field(default=MessageDataType.NODE,description="消息类型")
    data: NodeData = Field(description="合并转发消息节点数据")

class NodeData(BaseModel):
    id: Optional[str] = Field(description="合并转发消息节点ID")
    user_id: Optional[int] = Field(description="合并转发消息节点发送者QQ号")
    uin: Optional[str | int] = Field(description="合并转发消息节点发送者QQ号")
    nickname: str = Field(description="合并转发消息节点昵称")
    name: Optional[str] = Field(description="合并转发消息节点名称")
    content: MessageMixType = Field(description="合并转发消息节点内容")
    source: Optional[str] = Field(description="合并转发消息节点来源")
    news: Optional[list[TextData]] = Field(description="合并转发消息节点新闻")
    summary: Optional[str] = Field(description="合并转发消息节点摘要")
    prompt: Optional[str] = Field(description="合并转发消息节点提示")
    time: Optional[str] = Field(description="合并转发消息节点时间")

class MessageIdMusic(BaseModel):
    type: Literal[MessageDataType.MUSIC] = Field(default=MessageDataType.MUSIC,description="消息类型")
    data: Any = Field(description="音乐数据")

class MessageJson(BaseModel):
    type: Literal[MessageDataType.JSON] = Field(default=MessageDataType.JSON,description="消息类型")
    data: JsonData = Field(description="JSON数据")

class JsonData(BaseModel):
    config: Optional[JsonConfig] = Field(default=None,description="配置")
    data: str | object = Field(description="数据")

class JsonConfig(BaseModel):
    token: Optional[str] = Field(default=None,description="token")


class MessageDice(BaseModel):
    type: Literal[MessageDataType.DICE] = Field(default=MessageDataType.DICE,description="消息类型")
    data: Optional[DiceData] = Field(default=None,description="骰子数据")

class DiceData(BaseModel):
    result: int | str = Field(description="结果")

class MessageRPS(BaseModel):
    type: Literal[MessageDataType.RPS] = Field(default=MessageDataType.RPS,description="消息类型")

class MessageMarkdown(BaseModel):
    type: Literal[MessageDataType.MARKDOWN] = Field(default=MessageDataType.MARKDOWN,description="消息类型")
    data: MarkdownData = Field(description="Markdown数据")

class MarkdownData(BaseModel):
    content: str = Field(description="Markdown内容")

class MessageForward(BaseModel):
    type: Literal[MessageDataType.FORWARD] = Field(default=MessageDataType.FORWARD,description="消息类型")
    data: ForwardData = Field(description="合并转发消息数据")

class ForwardData(BaseModel):
    id: str = Field(description="合并转发消息ID")
    content: Optional[list[Message]] = Field(description="合并转发消息名称")

class MessagePoke(BaseModel):
    type: Literal[MessageDataType.POKE] = Field(default=MessageDataType.POKE,description="消息类型")
    data: PokeData = Field(description="戳一戳数据")

class PokeData(BaseModel):
    type: str = Field(description="戳一戳类型")
    id: str = Field(description="戳一戳ID")

# NOTE:
# typing.Annotated[T, ...] 必须至少包含两个参数（类型 + 注解/元数据）。
# 这里不需要额外元数据，直接用 Union 即可，避免 Python 3.13 抛 TypeError。
MessageData = Annotated[
    Union[
        MessageText,
        MessageFace,
        MessageMFace,
        MessageAt,
        MessageReply,
        MessageImage,
        MessageRecord,
        MessageFile,
        MessageVideo,
        MessageNode,
        MessageIdMusic,
        MessageJson,
        MessageDice,
        MessageRPS,
        MessageMarkdown,
        MessageForward,
        MessagePoke,
    ],
    # 关键：按消息段的 `type` 字段做判别联合，只验证命中的那一种，避免一堆无意义的报错
    Field(discriminator="type"),
]

MessageMixType = Annotated[Union[list[MessageData], str, MessageData], Field(description="消息混合类型")]

class Sender(BaseModel):
    """消息发送者信息"""
    user_id: int = Field(description="发送者QQ号")
    nickname: str = Field(description="昵称")
    sex: Optional[UserSex] = Field(default=None, description="性别")
    age: Optional[int] = Field(default=None, description="年龄")
    card: Optional[str] = Field(default=None, description="群名片")
    level: Optional[str] = Field(default=None, description="群等级")
    role: Optional[GroupMemberRole] = Field(default=None, description="角色")


# ----------------------------
# Pydantic v2 forward-ref 修复
# ----------------------------
# Message 在文件顶部定义，但其字段 `message: list[MessageData]` 在后面才声明。
# 同时 NodeData / ForwardData 也引用了后续类型（MessageMixType / Message）。
# 因此需要在所有类型定义完成后调用 model_rebuild()，否则运行时会报：
# `Message` is not fully defined; you should define `MessageData`, then call `Message.model_rebuild()`.
Message.model_rebuild()
NodeData.model_rebuild()
ForwardData.model_rebuild()