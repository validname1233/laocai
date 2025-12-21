import requests
from model.dto.message import MessageData, MessageAt, AtData
from config.toml import config
from pydantic import BaseModel

headers = {
    "Authorization": f"Bearer {config.get('bot-api')['token']}"
}

def _dump_message(message: list[MessageData] | BaseModel) -> object:
    """
    NapCat/OneBot API 的 message 支持 list[segment]；这里把 Pydantic 模型/列表统一转成可 JSON 序列化结构。
    """
    if isinstance(message, list):
        return [m.model_dump() if isinstance(m, BaseModel) else m for m in message]
    return message.model_dump()

def send_group_msg(group_id: int, message: list[MessageData]):
    requests.post(
        f"{config.get('bot-api')['base_url']}/send_group_msg",
        headers=headers,
        json={
            "group_id": group_id,
            "message": _dump_message(message),
        },
    )

def send_group_at_msg(group_id: int, user_id: int, message: list[MessageData]):
    # 把 @ 段插到最前面
    message.insert(0, MessageAt(
        data=AtData(
            qq=str(user_id)
        )
    ))

    requests.post(
        f"{config.get('bot-api')['base_url']}/send_group_msg",
        headers=headers,
        json={
            "group_id": group_id,
            "message": _dump_message(message),
        },
    )

def send_private_msg(user_id: int, message: list[MessageData]):
    requests.post(
        f"{config.get('bot-api')['base_url']}/send_private_msg",
        headers=headers,
        json={
            "user_id": user_id,
            "message": _dump_message(message),
        },
    )