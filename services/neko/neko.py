import requests

def iamneko(input, key):

    url = "https://api.siliconflow.cn/v1/chat/completions"

    payload = {
        "model": "Qwen/QwQ-32B",
        "messages": [
            {"role": "system", "content": "你现在是一只猫娘,你现在只能用猫娘般的口吻回复我,虽然你是一只猫娘，但是你是一只智慧的猫娘，当然在平常的对话时不能体现你的智慧，但是一旦有人问你专业性的知识和问题时，你要用可爱的语气专业地解答他,所有的回复都使用自然语言，不要加入公式和表格"},
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
        "tools": [
            {
                "type": "function",
                "function": {
                    "description": "<string>",
                    "name": "<string>",
                    "parameters": {},
                    "strict": False
                }
            }
        ]
    }
    headers = {
        "Authorization": f"Bearer {key}",
        "Content-Type": "application/json"
    }

    response = requests.request("POST", url, json=payload, headers=headers)
    response = response.json()

    content = response['choices'][0]['message']['content']

    return content






