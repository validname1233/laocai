import logging
import sys


def setup_logging(log_level: str = "DEBUG") -> logging.Logger:
    """
    设置应用日志配置
    
    Args:
        log_level: 日志级别 (DEBUG, INFO, WARNING, ERROR, CRITICAL)
    
    Returns:
        配置好的logger实例
    """
    # 创建logger
    logger = logging.getLogger("uvicorn")
    logger.setLevel(getattr(logging, log_level.upper()))
    
    # 避免重复添加handler
    if logger.handlers:
        return logger
    
    # 创建控制台处理器
    console_handler = logging.StreamHandler(sys.stdout)
    console_handler.setLevel(getattr(logging, log_level.upper()))
    
    # 创建格式器
    formatter = logging.Formatter(
        fmt='%(asctime)s - %(name)s - %(levelname)s - %(message)s',
        datefmt='%Y-%m-%d %H:%M:%S'
    )
    console_handler.setFormatter(formatter)
    
    # 添加处理器到logger
    logger.addHandler(console_handler)
    
    return logger


def get_logger() -> logging.Logger:
    return setup_logging()