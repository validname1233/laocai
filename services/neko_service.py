import requests
from typing import List
from configs.logging_config import get_logger
from configs.app_config import APPConfig
from models import GroupAtMessage
from services.message_service import MessageService

logger = get_logger()

class NekoService(MessageService):
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

    """猫娘AI服务类"""
    def __init__(self, app_config: APPConfig = None):
        self.app_config = app_config
        self.model = "Qwen/QwQ-32B"
        self.system_prompt = (
            "你现在是一只名字叫牢财的猫娘,你现在只能用猫娘般的口吻回复我,虽然你是一只猫娘，但是你是一只智慧的猫娘，"
            "当然在平常的对话时不能体现你的智慧，但是一旦有人问你专业性的知识和问题时，你要用可爱的语气专业地解答他,"
            "所有的回复都使用自然语言，不要加入公式和表格。另外，每次对话我会给你一段标注序号的文字，"
            "分别代表前几次对话的内容，如：1.我能摸摸你的尾巴吗，你要记忆前几次对话的内容，但只针对最新的一次(也就是序号最大的)给出回答"
        )
        self.chat_message_memory = self.ChatMessageMemory()
    
    async def _generate_response(self, user_input: str) -> str:
        """
        生成猫娘AI响应
        
        Args:
            user_input: 用户输入内容
            api_key: API密钥
            
        Returns:
            AI生成的响应内容
            
        Raises:
            Exception: API调用失败时抛出异常
        """
        payload = {
            "model": self.model,
            "messages": [
                {"role": "system", "content": self.system_prompt},
                {"role": "user", "content": user_input}
            ],
            "stream": False,
            "max_tokens": 512,
            "thinking_budget": 4096,
            "min_p": 0.05,
            "stop": None,
            "temperature": 0.7,
            "top_p": 0.7,
            "top_k": 50,
            "frequency_penalty": 0.5,
            "n": 1,
            "response_format": {"type": "text"},
        }
        
        headers = {
            "Authorization": f"Bearer {self.app_config.api_key}",
            "Content-Type": "application/json"
        }
        
        try:
            logger.debug(f"调用猫娘AI服务，输入长度: {len(user_input)}")
            
            response = requests.post(self.app_config.api_url, json=payload, headers=headers, timeout=30)
            response.raise_for_status()
            
            result = response.json()
            
            if 'choices' not in result or not result['choices']:
                raise Exception(f"API响应格式错误: {result}")
            
            content = result['choices'][0]['message']['content']
            
            logger.info(f"成功生成AI响应，长度: {len(content)}")
            return content
            
        except requests.Timeout:
            logger.error("API调用超时")
            return "喵~ 主人，服务器有点忙呢，请稍后再试试吧~"
        except requests.RequestException as e:
            logger.error(f"API请求失败: {str(e)}")
            return "喵~ 主人，我现在有点不舒服，请稍后再试试吧~"
        except KeyError as e:
            logger.error(f"API响应解析失败: {str(e)}")
            return "喵~ 主人，我没有理解你的意思呢~"
        except Exception as e:
            logger.error(f"生成AI响应异常: {str(e)}")
            return "喵~ 主人，出现了未知错误呢~"
    
    async def handle(self, access_token: str, msg: GroupAtMessage) -> bool:
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
        ai_response = await self._generate_response(memory_content)
        
        # 发送消息
        await self._send_message(access_token, msg.group_openid, ai_response, msg.id)
        
        return True