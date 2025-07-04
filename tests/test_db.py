# 导入SQLAlchemy的核心模块
from sqlalchemy import create_engine, Column, Integer, String
from sqlalchemy.orm import sessionmaker
from sqlalchemy.ext.declarative import declarative_base

# 定义数据库连接URL - 连接到本地MySQL数据库
DATABASE_URL = "mysql+mysqldb://root:123456@localhost:3306/laocai_db"

# 创建数据库引擎，配置连接池和日志选项
engine = create_engine(DATABASE_URL, echo=True, pool_recycle=3600, pool_pre_ping=True)

# 创建会话工厂，用于管理数据库会话
SessionFactory = sessionmaker(bind=engine, expire_on_commit=False)

# 创建声明式基类，用于定义数据模型
Base = declarative_base()

# 定义学生数据模型类
"""对应表student
id 主键
name 姓名
"""
class Student(Base):
    __tablename__ = "student"  # 指定数据表名称
    id = Column(Integer, primary_key=True)  # 主键，自动递增
    name = Column(String(20), nullable=False)  # 姓名字段，不能为空


# 使用会话工厂创建数据库会话并添加学生数据
with SessionFactory() as session:
    with session.begin():  # 开启事务
        session.add(Student(name="Bob"))  # 添加名为"Bob"的学生记录
    
