"""
测试配置依赖注入
展示配置管理使用依赖注入的优势
"""

import pytest
import tempfile
import os
from unittest.mock import patch
from configs.app_config import Settings, get_settings, clear_settings_cache
from app.dependencies import get_app_settings, clear_all_caches


class TestConfigDependencyInjection:
    """测试配置依赖注入"""
    
    def setup_method(self):
        """每个测试前清理缓存"""
        clear_all_caches()
    
    def test_different_config_files(self):
        """测试使用不同配置文件"""
        # 创建临时配置文件
        with tempfile.NamedTemporaryFile(mode='w', suffix='.yaml', delete=False) as f:
            f.write("""
appid: "test_app_id"
secret: "test_secret"
neko_key: "test_neko_key"
base_url: "https://api.test.com"
log_level: "INFO"
""")
            test_config_path = f.name
        
        try:
            # 使用特定配置文件
            settings1 = get_settings(test_config_path)
            assert settings1.appid == "test_app_id"
            assert settings1.base_url == "https://api.test.com"
            
            # 可以同时支持不同的配置文件
            # 这是全局变量模式做不到的！
            
        finally:
            os.unlink(test_config_path)
    
    def test_environment_based_config(self):
        """测试基于环境的配置"""
        with patch.dict(os.environ, {
            'APPID': 'env_app_id',
            'SECRET': 'env_secret',
            'NEKO_KEY': 'env_neko_key',
            'BASE_URL': 'https://env.api.com',
            'LOG_LEVEL': 'WARNING'
        }):
            from configs.app_config import get_settings_from_env
            settings = get_settings_from_env()
            
            assert settings.appid == 'env_app_id'
            assert settings.base_url == 'https://env.api.com'
            assert settings.log_level == 'WARNING'
    
    def test_mock_config_in_tests(self):
        """展示如何在测试中使用Mock配置"""
        # 创建测试专用配置
        test_settings = Settings(
            appid="test_app",
            secret="test_secret",
            neko_key="test_key", 
            base_url="https://test.api.com",
            log_level="DEBUG"
        )
        
        # 可以直接传递给需要配置的函数
        def some_function_needs_config(settings: Settings):
            return f"使用API: {settings.base_url}"
        
        result = some_function_needs_config(test_settings)
        assert "test.api.com" in result
    
    def test_config_validation(self):
        """测试配置验证"""
        # Pydantic会自动验证配置
        with pytest.raises(ValueError):
            Settings(
                appid="",  # 空值应该报错
                secret="test",
                neko_key="test"
            )


class TestDependencyFactoryPattern:
    """测试依赖工厂模式"""
    
    def test_environment_specific_settings(self):
        """测试环境特定的配置创建"""
        from app.dependencies import DependencyFactory
        
        # 模拟不同环境的配置文件
        test_configs = {
            'config.dev.yaml': {
                'base_url': 'https://dev.api.com',
                'log_level': 'DEBUG'
            },
            'config.prod.yaml': {
                'base_url': 'https://prod.api.com', 
                'log_level': 'WARNING'
            }
        }
        
        # 这种灵活性是全局变量模式很难实现的
        for env in ['dev', 'prod']:
            # 在实际应用中，工厂会根据环境加载不同配置
            pass  # 这里只是展示概念
    
    def test_service_with_custom_config(self):
        """测试根据配置创建定制服务"""
        from app.dependencies import DependencyFactory
        
        # 创建带有特殊API URL的配置
        custom_settings = Settings(
            appid="test",
            secret="test", 
            neko_key="test",
            base_url="https://custom.api.com"
        )
        
        # 根据配置创建服务
        service = DependencyFactory.create_neko_service_with_config(custom_settings)
        
        # 验证服务使用了正确的配置
        # 这里只是展示概念，实际测试需要检查服务内部状态


# 对比全局变量模式的问题示例
"""
❌ 全局变量模式的问题：

# 全局变量模式 - 很难测试不同场景
_global_settings = None

def get_global_settings():
    global _global_settings
    if _global_settings is None:
        _global_settings = Settings.load_from_file()
    return _global_settings

问题：
1. 只能有一个配置实例
2. 测试时无法使用不同配置
3. 不能同时支持多环境
4. Mock配置很困难

✅ 依赖注入的优势：

@lru_cache()
def get_settings(config_path: str = "config.yaml") -> Settings:
    return Settings.load_from_file(config_path)

# FastAPI中的依赖注入
async def api_handler(settings: Settings = Depends(get_app_settings)):
    # 自动注入配置，易于测试和Mock

优势：
1. 支持多种配置来源
2. 易于测试和Mock
3. 明确的依赖关系
4. 可以根据参数创建不同实例
""" 