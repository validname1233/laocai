import requests
import asyncio

import mysql.connector

from configs.logging_config import get_logger
from configs.app_config import APPConfig
from services.message_service import MessageService

class DBService(MessageService):
    """数据库服务类"""
    def __init__(self, app_config: APPConfig = None):
        self.app_config = app_config
        
    async def _generate_response(self) -> str:

        # 创建数据库连接
        db1 = mysql.connector.connect(
            host="localhost",    # 数据库服务器地址
            user="root", # 用户名
            password="1", # 密码
            database="database"    # 数据库名(可选)
        )

        cursor1 = db1.cursor()



#logger = get_logger()

if __name__ == "__main__":
    db1 = mysql.connector.connect(
            host="localhost",    # 数据库服务器地址
            user="zyh", # 用户名
            password="asd60asd", # 密码
            database="test"    # 数据库名(可选)
        )

    cursor1 = db1.cursor()


