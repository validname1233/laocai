import re
import random
import requests
from typing import List
from models import GroupAtMessage, GroupAtMessageResponse
from configs.logging_config import get_logger
from services.neko_service import NekoAIService
from configs.app_config import APPConfig

logger = get_logger()

class MessageService:
    class ChatMessageMemory:
        """消息记忆管理类"""
        
        def __init__(self, max_size: int = 5):
            self.max_size = max_size
            self.memory: List[str] = []
        
        def add(self, content: str):
            """
            添加消息到记忆中
            
            Args:
                content: 消息内容
                
            Returns:
                是否成功添加（False表示消息已存在）
            """
            self.memory.append(content)
            
            # 保持最大容量
            if len(self.memory) > self.max_size: self.memory.pop(0)
        
        def get_memory_content(self) -> str:
            """获取记忆内容的格式化字符串"""
            content_all = ""
            for i, content in enumerate(self.memory, 1):
                content_all += f"{i}.{content}; "
            return content_all
        
    def __init__(self, app_config: APPConfig = None, neko_service: NekoAIService = None):
        self.app_config = app_config
        self.prev_msg_ids = [] # 全局消息ID序列
        self.chat_message_memory = self.ChatMessageMemory() # 全局LLM记忆实例
        self.neko_service = neko_service   

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

    async def _handle_chat_command(self, access_token: str, msg: GroupAtMessage) -> bool:
        """
        处理聊天命令 (/说话)
        
        Returns:
            是否处理了该命令
        """
        if len(msg.content) < 5 or not msg.content.startswith(" /说话 "): return False
        
        self.chat_message_memory.add(msg.content[4:]) # 去掉 " /说话 "
        
        # 获取记忆内容并生成响应
        memory_content = self.chat_message_memory.get_memory_content()
        logger.debug(f"当前记忆内容: {self.chat_message_memory.memory}")
        logger.debug(f"生成AI响应的输入: {memory_content}")
        
        # 生成AI响应
        ai_response = self.neko_service.generate_response(memory_content)
        
        # 发送消息
        await self._send_message(access_token, msg.group_openid, ai_response, msg.id)
        
        return True


    async def _handle_dice_command(self, access_token: str, msg: GroupAtMessage) -> bool:
        """
        处理掷骰命令 (r{数量}d{面数})
        
        Returns:
            是否处理了该命令
        """
        dice_match = re.fullmatch(r"r(\d+)d(\d+)", msg.content.strip())
        if not dice_match: return False
        
        num_dice = int(dice_match.group(1))
        dice_sides = int(dice_match.group(2))
        
        # 限制掷骰数量和面数
        if num_dice > 100 or dice_sides > 1000:
            response = "掷骰数量或面数过大，请重新输入"
        else:
            results = [random.randint(1, dice_sides) for _ in range(num_dice)]
            response = f"掷骰结果: {results}, 总和: {sum(results)}"
        
        await self._send_message(access_token, msg.group_openid, response, msg.id)
        
        return True
    
    async def handle(self, access_token: str, d: dict):
        """
        处理群组@消息
        
        Args:
            access_token: 访问令牌
            payload_data: 消息负载数据
        """
        try:
            logger.debug(f"处理群组@消息: {d}")
            
            # 解析消息数据
            msg = GroupAtMessage(**d)

            # 检查消息是否重复
            logger.debug(f"当前消息ID序列: {self.prev_msg_ids}")
            if msg.id in self.prev_msg_ids:
                logger.debug(f"重复消息，跳过处理: {msg.id}")
                return
            
            self.prev_msg_ids.append(msg.id)
            
            # 处理聊天命令
            if await self._handle_chat_command(access_token, msg): return
            
            # 处理掷骰命令
            if await self._handle_dice_command(access_token, msg): return

            await self._send_message(access_token, msg.group_openid, f"牢财不知道你在说什么喵", msg.id)
                
            logger.debug("未匹配到任何命令处理器")
            
        except Exception as e:
            logger.error(f"处理群组@消息失败: {str(e)}")
            raise