import re
import random
from services.message_service import MessageService
from models import GroupAtMessage

class DiceService(MessageService):
    async def handle(self, access_token: str, msg: GroupAtMessage) -> bool:
        """
        处理掷骰命令 (r{数量}d{面数})
        
        Returns:
            是否处理了该命令
        """
        dice_match = re.fullmatch(r"r(\d+)d(\d+)", msg.content.strip())
        if not dice_match: return False
        
        num_dice = int(dice_match.group(1))
        dice_sides = int(dice_match.group(2))
        
        # 限制掷骰数量和面数
        if num_dice > 100 or dice_sides > 1000:
            response = "掷骰数量或面数过大，请重新输入"
        else:
            results = [random.randint(1, dice_sides) for _ in range(num_dice)]
            response = f"掷骰结果: {results}, 总和: {sum(results)}"
        
        await self._send_message(access_token, msg.group_openid, response, msg.id)
        
        return True