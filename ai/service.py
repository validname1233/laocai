from langchain.messages import RemoveMessage
from loguru import logger

# 导入上面的配置函数
from api.message import send_group_at_msg
from model.dto.message import Message, MessageText, TextData
from ai.agent import PREAMBLE_MESSAGES, agent, REMOVE_ALL_MESSAGES


def ai_reponse(message: Message):
    mem_config = {"configurable": {"thread_id": str(message.sender.user_id)}}
    # 指令：重置对话（清空历史并重新注入 PREAMBLE）
    message_text = message.text().strip()
    if message_text == "开大脚":
        agent.update_state(
            mem_config,
            {
                "messages": [
                    RemoveMessage(id=REMOVE_ALL_MESSAGES),
                    *PREAMBLE_MESSAGES,
                ]
            },
        )
        send_group_at_msg(
            message.group_id,
            message.sender.user_id,
            [MessageText(data=TextData(text=" 哈！"))],
        )
    final_response = agent.invoke({"messages": [("user", message_text)]}, mem_config)
    send_group_at_msg(
        message.group_id,
        message.sender.user_id,
        [MessageText(data=TextData(text=" " + final_response["messages"][-1].content))],
    )
