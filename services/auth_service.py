import requests
from configs.logging_config import get_logger
from configs.app_config import APPConfig

logger = get_logger()

class AuthService:
    def __init__(self, app_config: APPConfig = None):
        self.app_config = app_config

    async def get_access_token(self) -> str:
        """
        获取访问令牌
        
        Returns:
            访问令牌字符串
        
        Raises:
        Exception: 获取令牌失败时抛出异常
        """
        try:
            logger.debug(f"请求访问令牌，appid: {self.app_config.appid}")
            response = requests.post("https://bots.qq.com/app/getAppAccessToken", json={
                "appId": self.app_config.appid,
                "clientSecret": self.app_config.secret
            })
            response.raise_for_status()
        
            result = response.json()
            access_token = result.get("access_token")
        
            if not access_token:
                raise Exception(f"获取访问令牌失败: {result}")
            
            logger.info("成功获取访问令牌")
            return access_token
        
        except requests.RequestException as e:
            logger.error(f"请求访问令牌失败: {str(e)}")
            raise Exception(f"网络请求失败: {str(e)}")
        except Exception as e:
            logger.error(f"获取访问令牌异常: {str(e)}")
            raise 
