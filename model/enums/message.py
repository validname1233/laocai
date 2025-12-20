from enum import Enum

class MessageDataType(str, Enum):
    """OneBot 11 消息数据类型枚举"""
    TEXT = 'text'
    IMAGE = 'image'
    MUSIC = 'music'
    VIDEO = 'video'
    VOICE = 'record'    # 注意：TS 里对应的是 'record'
    FILE = 'file'
    AT = 'at'
    REPLY = 'reply'
    JSON = 'json'
    FACE = 'face'
    MFACE = 'mface'     # 商城表情
    MARKDOWN = 'markdown'
    NODE = 'node'       # 合并转发消息节点
    FORWARD = 'forward' # 合并转发消息，用于上报
    XML = 'xml'
    POKE = 'poke'
    DICE = 'dice'
    RPS = 'rps'
    MINIAPP = 'miniapp' # json类
    CONTACT = 'contact'
    LOCATION = 'location'

class PicSubType(int, Enum):
    KNORMAL = 0,
    KCUSTOM = 1,
    KHOT = 2,
    KDIPPERCHART = 3,
    KSMART = 4,
    KSPACE = 5,
    KUNKNOW = 6,
    KRELATED = 7