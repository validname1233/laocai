import logging
import sys
from loguru import logger

# 1. 定义拦截器：将标准库 logging 转发到 Loguru
class InterceptHandler(logging.Handler):
    def emit(self, record):
        # 获取对应的 Loguru 级别
        try:
            level = logger.level(record.levelname).name
        except ValueError:
            level = record.levelno

        # 找到调用日志的原始位置
        frame, depth = logging.currentframe(), 2
        while frame.f_code.co_filename == logging.__file__:
            frame = frame.f_back
            depth += 1

        logger.opt(depth=depth, exception=record.exc_info).log(
            level, record.getMessage()
        )

# 2. 配置 Loguru
def setup_logging():
    # 移除 Loguru 默认的 handler
    logger.remove()
    
    # 添加控制台输出 (支持颜色)
    logger.add(
        sys.stderr,
        level="INFO",
        format="<green>{time:YYYY-MM-DD HH:mm:ss}</green> | <level>{level: <8}</level> | <cyan>{name}</cyan>:<cyan>{function}</cyan>:<cyan>{line}</cyan> - <level>{message}</level>"
    )

    # 添加文件输出 (异步写入，防止阻塞机器人)
    logger.add(
        "logs/bot.log",
        rotation="10 MB",      # 10MB 分割文件
        retention="7 days",    # 保留7天
        enqueue=True,          # 关键！异步写入，不卡主线程
        level="DEBUG",
        encoding="utf-8"
    )

    # 3. 替换 Uvicorn 的日志处理器
    # 截获这几个模块的日志
    loggers = ("uvicorn", "uvicorn.access", "uvicorn.error", "fastapi")
    logging.getLogger().handlers = [InterceptHandler()]
    
    for logger_name in loggers:
        logging_logger = logging.getLogger(logger_name)
        logging_logger.handlers = [InterceptHandler()]
        # 确保不向上传播，避免重复打印
        logging_logger.propagate = False