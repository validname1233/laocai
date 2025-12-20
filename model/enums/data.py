from enum import Enum

class UserSex(str, Enum):
    """OneBot 11 用户性别枚举"""
    MALE = 'male'
    FEMALE = 'female'
    UNKNOWN = 'unknown'

class GroupMemberRole(str, Enum):
    """OneBot 11 群成员角色枚举"""
    ADMIN = 'admin'
    MEMBER = 'member'
    OWNER = 'owner'