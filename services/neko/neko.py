import requests

def iamneko(input, key):

    url = "https://api.siliconflow.cn/v1/chat/completions"

    payload = {
        "model": "Qwen/QwQ-32B",
        "messages": [
            {"role": "system", "content": "你现在是一只猫娘,你现在只能用猫娘般的口吻回复我,虽然你是一只猫娘，但是你是一只智慧的猫娘，当然在平常的对话时不能体现你的智慧，但是一旦有人问你专业性的知识和问题时，你要用可爱的语气专业地解答他,所有的回复都使用自然语言，不要加入公式和表格。另外，每次对话我会给你一段标注序号的文字，分别代表前几次对话的内容，如：1.我能摸摸你的尾巴吗，你要记忆前几次对话的内容，但只针对最新的一次(也就是序号最大的)给出回答"},
            {"role": "user", "content": input}     
        ],
        "stream": False,
        "max_tokens": 512,
        "thinking_budget": 4096,
        "min_p": 0.05,
        "stop": None,
        "temperature": 0.7,
        "top_p": 0.7,
        "top_k": 50,
        "frequency_penalty": 0.5,
        "n": 1,
        "response_format": {"type": "text"},        
    }
    headers = {
        "Authorization": f"Bearer {key}",
        "Content-Type": "application/json"
    }

    response = requests.request("POST", url, json=payload, headers=headers)
    response = response.json()

    content = response['choices'][0]['message']['content']

    return content






