from cryptography.hazmat.primitives.asymmetric.ed25519 import Ed25519PrivateKey
import binascii
from configs.app_config import APPConfig

from configs.logging_config import get_logger
from models import VerifyEvent

logger = get_logger()

class VerificationCallbackUrlService:
    def __init__(self, app_config: APPConfig):
        self.app_config = app_config

    def verify(self, event: VerifyEvent) -> dict:
        """
        验证回调URL
    
        Args:
            bot_secret: 机器人密钥
            payload_data: 负载数据
        
        Returns:
            包含plain_token和signature的字典
        """
        try:
            logger.debug("开始验证回调URL")

            # 1. 将字符串重复直到足够长度
            seed = self.app_config.secret
            while len(seed.encode('utf-8')) < 32: seed += seed  # 重复字符串

            # 2. 取前32字节作为种子
            seed_bytes = seed.encode('utf-8')[:32]
        
            # 3. 创建私钥（使用 from_private_bytes）
            private_key = Ed25519PrivateKey.from_private_bytes(seed_bytes)
            plain_token= event.plain_token
            msg = (event.event_ts + plain_token).encode('utf-8')
            signature = private_key.sign(msg)
            signature_hex = binascii.hexlify(signature).decode('utf-8')

            return {
                "plain_token": plain_token,
                "signature": signature_hex
            }
        
        except Exception as e:
            logger.error(f"验证回调URL失败: {str(e)}")
            raise Exception(f"验证失败: {str(e)}") 