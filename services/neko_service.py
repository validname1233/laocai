import requests, queue, asyncio
from configs.logging_config import get_logger
from configs.app_config import APPConfig
from models import GroupAtMessage
from services.message_service import MessageService

logger = get_logger()

class NekoService(MessageService):
    """猫娘AI服务类"""
    def __init__(self, app_config: APPConfig = None):
        self.app_config = app_config
        self.SYSTEM_PROMPT = "你现在是一只名字叫牢财的猫娘,你现在只能用猫娘般的口吻回复我,虽然你是一只猫娘，但是你是一只智慧的猫娘，" + \
            "当然在平常的对话时不能体现你的智慧，但是一旦有人问你专业性的知识和问题时，你要用可爱的语气专业地解答他," + \
            "所有的回复都使用自然语言，不要加入公式和表格。" + \
            "你每次的回复都不要分超过3段。"
        
        self.messages: queue.Queue = queue.Queue(maxsize=10)

    
    async def _generate_response(self) -> str:
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
            "model": "Qwen/QwQ-32B",
            "messages": [
                {"role": "system", "content": self.SYSTEM_PROMPT},
                *self.messages.queue
            ],
            "stream": False,
            "max_tokens": 256,
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
            logger.debug(f"调用猫娘AI服务, 输入长度: {self.messages.qsize()}")
            
            response = requests.post(self.app_config.api_url, json=payload, headers=headers, timeout=30)
            response.raise_for_status()
            
            result = response.json()
            
            if 'choices' not in result or not result['choices']:
                raise Exception(f"API响应格式错误: {result}")
            
            content = result['choices'][0]['message']['content']

            if self.messages.qsize() >= 10: self.messages.get()
            self.messages.put({"role": "assistant", "content": content})
            
            logger.info(f"成功生成AI响应, 长度: {self.messages.qsize()}")
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
        
        if self.messages.qsize() >= 10: self.messages.get()
        self.messages.put({"role": "user", "content": msg.content[4:]}) # 去掉 " /说话 "
        
        logger.debug(f"生成AI响应的输入: {self.messages.queue}")

        ai_response = await self._generate_response()

        # 检查AI响应是否为空
        if not ai_response or not ai_response.strip():
            logger.warning("AI响应为空，使用默认回复")
            ai_response = "喵~ 主人，我现在有点迷糊呢~"

        # 分割响应并过滤空白部分
        response_parts = [part.strip() for part in ai_response.split("\n\n") if part.strip()]
        
        for i, ai_response_part in enumerate(response_parts):
            # 降低发送频率
            await asyncio.sleep(0.5)
            await self._send_message(access_token, msg.group_openid, ai_response_part, msg.id, msg_seq=i+1)
        
        return True