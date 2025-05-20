import asyncio
import sys
import os
import botpy
from botpy.ext.cog_yaml import read
from botpy.message import GroupMessage, Message

config = read("config.yaml")

class laocai(botpy.Client):    
    """async def on_group_at_message_create(self, message: GroupMessage):
        await message.reply(content=f"哈基米哈基米哈基米 哈基米哦拿没路多")"""

    async def on_group_at_message_create(self, message: GroupMessage):
        file_url = "https://i.postimg.cc/MKMVYxdm/laocai.jpg"  # 这里需要填写上传的资源Url
        uploadMedia = await message._api.post_group_file(
            group_openid=message.group_openid, 
            file_type=1, # 文件类型要对应上，具体支持的类型见方法说明
            url=file_url # 文件Url
        )

        # 资源上传后，会得到Media，用于发送消息
        await message._api.post_group_message(
            group_openid=message.group_openid,
            msg_type=7,  # 7表示富媒体类型
            msg_id=message.id, 
            media=uploadMedia
        )

intents = botpy.Intents(public_messages=True) 
client = laocai(intents=intents, is_sandbox=True)  #, is_sandbox=True
client.run(appid=config["appid"], secret=config["secret"])

