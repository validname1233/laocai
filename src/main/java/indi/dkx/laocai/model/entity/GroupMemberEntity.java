package indi.dkx.laocai.model.entity;

import indi.dkx.laocai.model.enums.Sex;

public record GroupMemberEntity(Long userId, String nickname, Sex sex) { }
