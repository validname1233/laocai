package indi.dkx.laocai.model.entity;

import indi.dkx.laocai.model.enums.Role;
import indi.dkx.laocai.model.enums.Sex;

public record GroupMemberEntity(
        Long userId, String nickname, Sex sex, Long groupId, String card, String title, Integer level, Role role,
        Long joinTime, Long lastSentTime, Long shutUpEndTime) { }
