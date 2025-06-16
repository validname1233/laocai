import requests
from configs.logging_config import get_logger
from configs.app_config import APPConfig

logger = get_logger()

class NekoService:
    """猫娘AI服务类"""
    def __init__(self, app_config: APPConfig):
        self.api_url = app_config.api_url
        self.api_key = app_config.api_key
        self.model = "Qwen/QwQ-32B"
        self.system_prompt = (
            "你现在是一只名字叫牢财的猫娘,你现在只能用猫娘般的口吻回复我,虽然你是一只猫娘，但是你是一只智慧的猫娘，"
            "当然在平常的对话时不能体现你的智慧，但是一旦有人问你专业性的知识和问题时，你要用可爱的语气专业地解答他,"
            "所有的回复都使用自然语言，不要加入公式和表格。另外，每次对话我会给你一段标注序号的文字，"
            "分别代表前几次对话的内容，如：1.我能摸摸你的尾巴吗，你要记忆前几次对话的内容，但只针对最新的一次(也就是序号最大的)给出回答"
        )
    
    def generate_response(self, user_input: str) -> str:
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
            "Authorization": f"Bearer {self.api_key}",
            "Content-Type": "application/json"
        }
        
        try:
            logger.debug(f"调用猫娘AI服务，输入长度: {len(user_input)}")
            
            response = requests.post(self.api_url, json=payload, headers=headers, timeout=30)
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