import yaml
import os
from pydantic import BaseModel


class APPConfig(BaseModel):
    """应用配置类"""
    model_config = {"frozen": True}
    
    appid: str
    secret: str
    api_url: str
    api_key: str
    base_url: str = "https://sandbox.api.sgroup.qq.com"
    
    @classmethod
    def load_from_file(cls, config_path: str = "config.yaml") -> "APPConfig":
        """从配置文件加载设置"""
        if not os.path.exists(config_path):
            raise FileNotFoundError(f"配置文件 {config_path} 不存在")
        
        with open(config_path, "r", encoding="utf-8") as f:
            config_data = yaml.safe_load(f)
        
        return cls(**config_data)