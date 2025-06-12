from fastapi import APIRouter, HTTPException
from ..models import Payload
from ..logging_config import get_logger
from ..dependencies import SettingsDep, NekoServiceDep
from ..services.auth_service import get_access_token
from ..services.message_service import handle_group_at_message
from ..services.verification_service import verify_callback_url

logger = get_logger()
router = APIRouter()


@router.get("/")
async def health_check():
    """健康检查接口"""
    return {"status": "ok", "message": "老蔡QQ机器人运行正常"}


@router.post("/webhook")
async def webhook_handler(
    payload: Payload,
    settings: SettingsDep,      # 依赖注入配置
    neko_service: NekoServiceDep  # 依赖注入服务
):
    """WebHook处理接口"""
    logger.debug(f"接收到WebHook请求: op={payload.op}, t={payload.t}")
    
    try:
        match payload.op:
            case 0:  # Dispatch 事件
                access_token = await get_access_token(settings.appid, settings.secret)
                
                match payload.t:
                    case "GROUP_AT_MESSAGE_CREATE":
                        await handle_group_at_message(
                            settings.base_url, 
                            access_token, 
                            payload.d, 
                            settings.neko_key,
                            neko_service
                        )
                        
            case 13:  # URL验证
                result = verify_callback_url(settings.secret, payload.d)
                return result
                
        return {"status": "success"}
        
    except Exception as e:
        logger.error(f"处理WebHook请求失败: {str(e)}")
        raise HTTPException(status_code=500, detail=f"内部服务器错误: {str(e)}")