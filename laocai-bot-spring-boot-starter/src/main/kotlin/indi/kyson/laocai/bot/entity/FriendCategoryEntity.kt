package indi.kyson.laocai.bot.entity

import com.fasterxml.jackson.annotation.JsonProperty

data class FriendCategoryEntity(
    @JsonProperty("category_id")
    val categoryId: Int,
    @JsonProperty("category_name")
    val categoryName: String,
)
