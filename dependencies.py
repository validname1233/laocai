"""
依赖注入容器
统一管理应用中的所有依赖
"""
from functools import lru_cache
from typing import Annotated
from fastapi import Depends

from configs.app_config import APPConfig
from services.neko_service import NekoAIService
from services.auth_service import AuthService
from services.verification_callback_url_service import VerificationCallbackUrlService
from services.message_service import MessageService

# === 配置依赖 ===
# 创建FastAPI依赖注解
@lru_cache()
def get_app_config(config_path: str = "config.yaml") -> APPConfig:
    """
    获取配置实例 - 使用依赖注入模式
    
    使用@lru_cache()确保单例模式，但比全局变量更安全
    可以通过不同的config_path参数创建不同的配置实例
    
    Args:
        config_path: 配置文件路径
        
    Returns:
        Settings实例
    """
    return APPConfig.load_from_file(config_path)

APPConfigDep = Annotated[APPConfig, Depends(get_app_config)]

# === 服务依赖 ===
@lru_cache()
def get_verification_callback_url_service(app_config: APPConfigDep) -> VerificationCallbackUrlService:
    """创建验证回调URL服务实例"""
    return VerificationCallbackUrlService(app_config)

VerificationCallbackUrlServiceDep = Annotated[VerificationCallbackUrlService, Depends(get_verification_callback_url_service)]

@lru_cache()
def get_auth_service(app_config: APPConfigDep) -> AuthService:
    """创建认证服务实例"""
    return AuthService(app_config)

AuthServiceDep = Annotated[AuthService, Depends(get_auth_service)]


@lru_cache()
def get_neko_service(app_config: APPConfigDep) -> NekoAIService:
    """创建猫娘AI服务实例"""
    return NekoAIService(app_config)

NekoServiceDep = Annotated[NekoAIService, Depends(get_neko_service)]

@lru_cache()
def get_message_service(app_config: APPConfigDep, neko_service: NekoServiceDep) -> MessageService:
    """创建消息服务实例"""
    return MessageService(app_config, neko_service)

MessageServiceDep = Annotated[MessageService, Depends(get_message_service)]

# === 清理函数（用于测试）===

def clear_all_caches():
    """清除所有依赖缓存"""
    get_app_config.cache_clear()
    get_neko_service.cache_clear()
    get_verification_callback_url_service.cache_clear()
    get_message_service.cache_clear()  
    get_auth_service.cache_clear()