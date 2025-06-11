from typing import Optional

from pydantic import BaseModel, Field
import re, random, requests
from .neko.neko import iamneko
"""
这样前面带.的引用方式叫相对引用,通常用在一个包的内部,如工程文件夹所示,本文件所在的services内有一个__init__.py文件,因此这个文件算作一个包,
当这个包被引用的时候,.表示这个包的根目录,因此.neko.neko的写法是正确的
而from neko.neko是找不到文件的,因为这种引用方式是绝对引用,是从项目的根目录开始查找的,如果改成from services.neko.neko import iamneko就不会报错了

"""
from . import logger

#from openai import AsyncOpenAI
#from openai.types.chat import ChatCompletionUserMessageParam, ChatCompletionAssistantMessageParam

class GroupAtMessageCreateData(BaseModel):
    id: str #
    content: str  #
    timestamp: str  #
    author: object #
    group_openid: str
    attachments: Optional[list[object]] = Field(default=None, description="<UNK>")
    group_id: str
    message_scene: object
    message_type: int


async def on_group_at_message_create(base_url: str, access_token: str, d: dict, neko_key: str):
    logger.debug(d)
    data = GroupAtMessageCreateData(**d)

    if(len(data.content)>=5):
        if(data.content[:5] == " /说话 "):
            content_neko = iamneko(data.content[4:], neko_key)
            response = requests.post(f"{base_url}/v2/groups/{data.group_openid}/messages",
                                headers={"Authorization": f"QQBot {access_token}"},
                                json={
                                    "content": content_neko,
                                    "msg_id": data.id,
                                    "msg_type": 0,
                                })

    dice = re.fullmatch(r"r(\d+)d(\d+)", data.content.lstrip())
    if dice:
        num_dice = int(dice.group(1))  # 1
        dice_sides = int(dice.group(2))  # 100
        results = [random.randint(1, dice_sides) for _ in range(num_dice)]
        response = requests.post(f"{base_url}/v2/groups/{data.group_openid}/messages",
                            headers={"Authorization": f"QQBot {access_token}"},
                            json={
                                "content": f"掷骰结果: {results},总和: {sum(results)}",
                                "msg_id": data.id,
                                "msg_type": 0,
                            })
        logger.debug(response.json())
        
    #print("00000\n00000")
    