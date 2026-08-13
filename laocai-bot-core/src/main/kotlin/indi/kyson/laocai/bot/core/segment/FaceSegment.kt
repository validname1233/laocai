package indi.kyson.laocai.bot.core.segment

import com.fasterxml.jackson.annotation.JsonIgnore

/**
 * 表情消息段。
 */
class FaceSegment private constructor(override val data: Data) : Segment {

    data class Data(val faceId: String, val isLarge: Boolean) {
        fun toSegment(): FaceSegment = FaceSegment(this)
    }

    override val type: String
        get() = "face"

    @get:JsonIgnore
    val faceId: String
        get() = data.faceId

    @get:JsonIgnore
    val isLarge: Boolean
        get() = data.isLarge

    companion object {
        @JvmStatic
        fun of(faceId: String, isLarge: Boolean): FaceSegment = FaceSegment(Data(faceId, isLarge))
    }
}
