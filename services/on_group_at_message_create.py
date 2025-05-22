from typing import Optional

from pydantic import BaseModel, Field
import re, random, requests
from . import logger
from openai import AsyncOpenAI
from openai.types.chat import ChatCompletionUserMessageParam, ChatCompletionAssistantMessageParam

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


async def on_group_at_message_create(base_url: str, access_token: str, d: dict):
    logger.debug(d)
    data = GroupAtMessageCreateData(**d)

    dice = re.fullmatch(r"r(\d+)d(\d+)", data.content.lstrip())
    if dice:
        num_dice = int(dice.group(1))  # 1
        dice_sides = int(dice.group(2))  # 100
        results = [random.randint(1, dice_sides) for _ in range(num_dice)]
        response = requests.post(f"{base_url}/v2/groups/{data.group_openid}/messages",
                            headers={"Authorization": f"QQBot {access_token}"},
                            json={
                                "content": f"掷骰结果: {results}，总和: {sum(results)}",
                                "msg_id": data.id,
                                "msg_type": 0,
                            })
        logger.debug(response.json())

    else:
        client = AsyncOpenAI(base_url="https://api.siliconflow.cn/v1/", api_key="sk-ifjaclichnjgweydafulpcpgqnzyyxgvjvcqvmdptoogtycn")
        model_answer = await client.chat.completions.create(
            model="Pro/deepseek-ai/DeepSeek-R1",
            messages=[ChatCompletionAssistantMessageParam(role="assistant", content="only output pure text instead of markdown"),
                      ChatCompletionUserMessageParam(content=data.content, role="user")]
        )
        response = requests.post(f"{base_url}/v2/groups/{data.group_openid}/messages",
                            headers={"Authorization": f"QQBot {access_token}"},
                            json={
                                "content": f"{model_answer.choices[0].message.content}",
                                "msg_id": data.id,
                                "msg_type": 0,
                            })
        logger.debug(response.json())