import re
import random
import requests
from typing import List
from models import GroupAtMessage, GroupAtMessageResponse
from configs.logging_config import get_logger
from configs.app_config import APPConfig

logger = get_logger()

class MessageService:   
    def __init__(self, app_config: APPConfig = None):
        self.prev_msg_ids = [] # 全局消息ID序列
        self.app_config = app_config
        self.services = [] # 服务列表

    async def _send_message(self, access_token: str, group_openid: str, content: str, msg_id: str):
        """
        发送消息到群组
        
        Args:
            access_token: 访问令牌
            group_openid: 群组OpenID
            content: 消息内容
            msg_id: 回复的消息ID
        """
        try:
            url = f"{self.app_config.base_url}/v2/groups/{group_openid}/messages"
            headers = {"Authorization": f"QQBot {access_token}"}
            
            resp = GroupAtMessageResponse(
                content=content,
                msg_id=msg_id,
                msg_type=0
            )
            
            logger.debug(f"发送消息到群组 {group_openid}: {content}")
            
            response = requests.post(url, headers=headers, json=resp.model_dump())
            response.raise_for_status()
            
            logger.info(f"成功发送消息: {response.json()}")
            
        except requests.RequestException as e:
            logger.error(f"发送消息失败: {str(e)}")
            raise Exception(f"消息发送失败: {str(e)}")
        except Exception as e:
            logger.error(f"发送消息异常: {str(e)}")
            raise 
    
    async def handle(self, access_token: str, msg: GroupAtMessage):
        """
        处理群组@消息
        
        Args:
            access_token: 访问令牌
            payload_data: 消息负载数据
        """
        try:
            logger.debug(f"处理群组@消息: {msg}")
            
            # 检查消息是否重复
            logger.debug(f"当前消息ID序列: {self.prev_msg_ids}")
            if msg.id in self.prev_msg_ids:
                logger.debug(f"重复消息，跳过处理: {msg.id}")
                return
            
            self.prev_msg_ids.append(msg.id)
            
            # 遍历所有服务，如果某个服务返回True，则表示该服务处理了消息，则跳出循环
            for service in self.services:
                if await service.handle(access_token, msg): return

            await self._send_message(access_token, msg.group_openid, f"牢财不知道你在说什么喵", msg.id)
                
            logger.debug("未匹配到任何命令处理器")
            
        except Exception as e:
            logger.error(f"处理群组@消息失败: {str(e)}")
            raise