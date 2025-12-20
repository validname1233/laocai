from enum import Enum

class EventType(str, Enum):
    META = 'meta_event'
    REQUEST = 'request'
    NOTICE = 'notice'
    MESSAGE = 'message'
    MESSAGE_SENT = 'message_sent'