"""
依赖注入容器
统一管理应用中的所有依赖
"""

from functools import lru_cache
from typing import Annotated
from fastapi import Depends

from .services.neko_service import NekoAIService
from configs.app_config import Settings, get_settings


# === 配置依赖 ===

def get_app_settings() -> Settings:
    """获取应用配置 - FastAPI依赖"""
    return get_settings()


# 创建FastAPI依赖注解
SettingsDep = Annotated[Settings, Depends(get_app_settings)]


# === 服务依赖 ===

@lru_cache()
def create_neko_service() -> NekoAIService:
    """创建猫娘AI服务实例"""
    return NekoAIService()


def get_neko_service() -> NekoAIService:
    """获取猫娘AI服务实例 - FastAPI依赖"""
    return create_neko_service()


# 创建FastAPI依赖注解
NekoServiceDep = Annotated[NekoAIService, Depends(get_neko_service)]


# === 清理函数（用于测试）===

def clear_all_caches():
    """清除所有依赖缓存"""
    from configs.app_config import clear_settings_cache
    clear_settings_cache()
    create_neko_service.cache_clear()


# === 依赖工厂（用于更复杂的场景）===

class DependencyFactory:
    """依赖工厂类 - 用于需要运行时参数的依赖"""
    
    @staticmethod
    def create_neko_service_with_config(settings: Settings) -> NekoAIService:
        """根据配置创建服务实例"""
        # 可以根据配置使用不同的API URL
        api_url = getattr(settings, 'neko_api_url', 'https://api.siliconflow.cn/v1/chat/completions')
        return NekoAIService(api_url=api_url)
    
    @staticmethod
    def create_settings_for_env(env: str) -> Settings:
        """为特定环境创建配置"""
        config_files = {
            'dev': 'config.dev.yaml',
            'test': 'config.test.yaml',
            'prod': 'config.prod.yaml'
        }
        config_path = config_files.get(env, 'config.yaml')
        return get_settings(config_path) 