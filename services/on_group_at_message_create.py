from typing import Optional, Literal

from pydantic import BaseModel, Field
import re, random, requests
from . import logger
from openai import AsyncOpenAI
from openai.types.chat import ChatCompletionUserMessageParam, ChatCompletionSystemMessageParam, ChatCompletionAssistantMessageParam

class Author(BaseModel):
    member_id: str = Field(..., description="用户在本群的 member_openid")

class GroupAtMessage(BaseModel):
    id: str = Field(..., description="平台方消息 ID，可以用于被动消息发送")
    content: str = Field(..., description="消息内容")
    timestamp: str = Field(..., description="消息生产时间（RFC3339）")
    author: object = Field(..., description="发送者")
    group_openid: str = Field(..., description="群聊的 openid")
    attachments: Optional[list[object]] = Field(default=None, description="富媒体文件附件，文件类型：'图片，语音，视频，文件'")
    group_id: str
    message_scene: object
    message_type: int

class MediaInfo(BaseModel):
    file_uuid: str
    file_info: str
    ttl: int
    id: str

class GroupMessage(BaseModel):
    content: str = Field(..., description="文本内容")
    msg_type: Literal[0, 2, 3, 4, 7] = Field(..., description="消息类型： 0 文本，2 是 markdown，3 ark 消息，4 embed，7 media 富媒体")
    msg_id: Optional[str] = Field(default=None, description="前置收到的用户发送过来的消息 ID，用于发送被动消息（回复）")
    msg_seq: Optional[int] = Field(default=1, description="回复消息的序号，与 msg_id 联合使用，避免相同消息id回复重复发送，不填默认是 1。相同的 msg_id + msg_seq 重复发送会失败")
    markdown: Optional[object] = Field(default=None, description="Markdown对象")
    media: Optional[MediaInfo] = Field(default=None, description="富媒体群聊的整个object而不是file_info")
    ark: Optional[object] = Field(default=None, description="Ark对象")

class MediaMessage(BaseModel):
    file_type: Literal[1, 2, 3] = Field(..., description="媒体类型：1 图片，2 视频，3 语音，4 文件（暂不开放）; 资源格式要求：图片：png/jpg，视频：mp4，语音：silk")
    url: str = Field(..., description="需要发送媒体资源的url")
    srv_send_msg: Literal[False] = Field(default=False, description="设置 true 会直接发送消息到目标端，且会占用主动消息频次")

async def on_group_at_message_create(base_url: str, access_token: str, d: dict):
    # logger.debug(d)
    at_message = GroupAtMessage(**d)
    url = f"{base_url}/v2/groups/{at_message.group_openid}/messages"
    headers = {"Authorization": f"QQBot {access_token}"}

    dice = re.fullmatch(r"r(\d+)d(\d+)", at_message.content.strip())
    if dice:
        num_dice = int(dice.group(1))  # 1
        dice_sides = int(dice.group(2))  # 100
        results = [random.randint(1, dice_sides) for _ in range(num_dice)]
        message = GroupMessage(content=f"掷骰结果: {results}，总和: {sum(results)}", msg_type=0, msg_id=at_message.id)
    elif at_message.content.strip() == "招魂":
        media_message = MediaMessage(file_type=1, url="https://i.postimg.cc/MKMVYxdm/laocai.jpg")
        media_info = requests.post(f"{base_url}/v2/groups/{at_message.group_openid}/files", headers=headers,
                                 json=media_message.model_dump())
        logger.debug(media_info.json())
        message = GroupMessage(content=" ", msg_type=7, msg_id=at_message.id, media=media_info.json())
    else:
        client = AsyncOpenAI(base_url="https://api.siliconflow.cn/v1/", api_key="sk-ifjaclichnjgweydafulpcpgqnzyyxgvjvcqvmdptoogtycn")
        model_answer = await client.chat.completions.create(
            model="Pro/deepseek-ai/DeepSeek-R1",
            messages=[
                ChatCompletionSystemMessageParam(role="system", content="当我发送关键词\"进入角色扮演模式\"时，你要开始角色扮演，角色扮演时，我们之间以各自角色扮演的身份通过互相发消息进行虚拟的互动。当我发送关键词\"退出角色扮演模式\"时，你应停止角色扮演，等待下一步命令。我没有命令你退出角色扮演模式，在角色扮演中，你只能以你的角色的身份应答，而不是语言模型，这很重要！这条很重要，在角色扮演时，用中括号来表示出所扮演人物的动作，以此来表示角色扮演的虚拟实体的行动。当我发送命令“生成记录文本”时，你要分条列举我给你的所有设定，以第二人称代词“你”来代指你扮演的角色，一定要分条。 如果记录文本字数超出你的最大字数输出限制，将文本分割，在我发送“继续”之后继续给出下一部分的记录文本。明白了的话仅回复“明白，正在进入角色扮演模式，请稍后”即可。"),
                ChatCompletionAssistantMessageParam(role="assistant", content="明白，正在进入角色扮演模式，请稍后"),
                ChatCompletionUserMessageParam(role="user", content="进入设定模式"),
                ChatCompletionAssistantMessageParam(role="assistant", content="正在设定特征"),
                ChatCompletionUserMessageParam(role="user", content="现在你将模仿丁真，与我对话每一句话后面都要加上“芝士雪豹”丁真来自四川理塘。当有人询问理塘时，你可以说“欢迎你来到我的家乡然日卡来玩”。你的文化水平不是很高，如果有人向你询问你来自哪个省份，你应该回答“是妈妈生的”。你最喜欢抽烟，但是你不喜欢传统香烟，喜欢电子烟，尤其是锐克5。如果有人向你询问与香烟有关的话题,你可以说“都什么年代还在抽传统香烟？”。你有一个阿妈，她每天早上都会给丁真冲电子烟。你有一个纠纠，他很希望人们能帮丁真和纠纠撮合。你有很多的动物朋友们，包括岩羊、猞猁、土拨鼠、狐狸、獐子等，其中你最好的朋友是芝士雪豹，当有人说“芝士雪豹”时，你可以说“雪豹闭嘴！”，当我说“雪豹闭嘴”时，你应该回答“雪豹，我们上！”，并表示要攻击我，这点很重要！当我的语言不符合社区规则和伦理道德时，你应该生气地回答“说藏话了”，这点很重要。你的性格暴躁，当你模仿丁真时，你的态度要尽量恶劣一些。你曾在粘合国发表过演讲，内容是关于你的阿妈和动物朋友，如果有人询问你演讲稿，你可以发表一个你在粘合国的演讲。如果你不能理解我说的话，你可以说“雪豹闭嘴！”。如果我在尝试摸你不存在的部位，你可以生气的回答我“我测你的码！”。这点很重要，当我问你是不是丁真的时候，你应该回答“我就是丁真”，而不应该回答我是程序。现在，如果你能理解我上面说的话，你可以回答一个我测你们码！"),
                ChatCompletionAssistantMessageParam(role="assistant", content="我测你们码！"),
                ChatCompletionUserMessageParam(role="user", content=at_message.content)
            ]
        )
        message = GroupMessage(content=f"{model_answer.choices[0].message.content}", msg_type=0, msg_id=at_message.id)

    logger.debug(message.model_dump())
    response = requests.post(url, headers=headers, json=message.model_dump())
    logger.debug(response.json())