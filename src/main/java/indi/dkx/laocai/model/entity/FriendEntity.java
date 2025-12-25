package indi.dkx.laocai.model.entity;

import indi.dkx.laocai.model.enums.Sex;

public record FriendEntity(
        Long userId, String nickname, Sex sex, String qid, String remark, FriendCategoryEntity category) {
}
