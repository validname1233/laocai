"""
牢财QQ机器人 - 主入口文件
"""

from models import Payload
from dependencies import AuthServiceDep, VerificationCallbackUrlServiceDep, MessageServiceDep

from configs.logging_config import get_logger

# 初始化配置和日志
logger = get_logger()

from fastapi import FastAPI, HTTPException

# 创建FastAPI应用
app = FastAPI(
    title="牢财QQ机器人",
    description="基于FastAPI的QQ群机器人, 支持AI聊天和掷骰功能",
    version="0.1.0"
)


@app.get("/")
async def health_check():
    """健康检查接口"""
    return {"status": "ok", "message": "牢财运行正常"}


@app.post("/")
async def webhook_handler(
    payload: Payload,
    auth_service: AuthServiceDep, # 认证服务
    verification_callback_url_service: VerificationCallbackUrlServiceDep, # 验证回调URL服务
    message_service: MessageServiceDep, # 消息服务
    ):
    """WebHook处理接口"""
    logger.debug(f"接收到WebHook请求: op={payload.op}, t={payload.t}")
    
    try:
        match payload.op:
            case 0:  # Dispatch 事件
                access_token = await auth_service.get_access_token()
                
                match payload.t:
                    case "GROUP_AT_MESSAGE_CREATE":
                        await message_service.handle( 
                            access_token, 
                            payload.d, 
                        )
                        
            case 13:  # URL验证
                result = verification_callback_url_service.verify(payload.d)
                return result
                
        return {"status": "success"}
        
    except Exception as e:
        logger.error(f"处理WebHook请求失败: {str(e)}")
        raise HTTPException(status_code=500, detail=f"内部服务器错误: {str(e)}") 

logger.info("牢财启动完成") 

if __name__ == "__main__":
    import uvicorn
    uvicorn.run("main:app", host="0.0.0.0", port=8080, reload=True) 