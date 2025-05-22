from pydantic import BaseModel

from cryptography.hazmat.primitives.asymmetric.ed25519 import Ed25519PrivateKey
import binascii

class VerifyEvent(BaseModel):
    plain_token: str
    event_ts: str

def verify_callback_url(bot_secret: str, d: dict):
    event = VerifyEvent(**d)
    # 1. 将字符串重复直到足够长度
    seed = bot_secret
    while len(seed.encode('utf-8')) < 32: seed += seed  # 重复字符串

    # 2. 取前32字节作为种子
    seed_bytes = seed.encode('utf-8')[:32]

    # 3. 创建私钥（使用 from_private_bytes）
    private_key = Ed25519PrivateKey.from_private_bytes(seed_bytes)
    plain_token= event.plain_token
    msg = (event.event_ts + plain_token).encode('utf-8')
    signature = private_key.sign(msg)
    signature_hex = binascii.hexlify(signature).decode('utf-8')

    return plain_token, signature_hex