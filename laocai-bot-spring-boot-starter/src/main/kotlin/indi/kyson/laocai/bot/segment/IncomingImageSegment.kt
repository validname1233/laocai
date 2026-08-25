package indi.kyson.laocai.bot.segment

import com.fasterxml.jackson.annotation.JsonIgnore
import indi.kyson.laocai.bot.enums.ImageSubType

/**
 * 入站的图片消息段。
 *
 * 只在反序列化时产生，携带协议返回的临时资源信息。
 */
class IncomingImageSegment private constructor(override val data: Data) : Segment {

    data class Data(
        val resourceId: String,
        val tempUrl: String,
        val width: Long,
        val height: Long,
        val summary: String,
        val subType: ImageSubType,
    ) {
        fun toSegment(): IncomingImageSegment = IncomingImageSegment(this)
    }

    override val type: String
        get() = "image"

    @get:JsonIgnore
    val resourceId: String
        get() = data.resourceId

    @get:JsonIgnore
    val tempUrl: String
        get() = data.tempUrl

    @get:JsonIgnore
    val width: Long
        get() = data.width

    @get:JsonIgnore
    val height: Long
        get() = data.height

    @get:JsonIgnore
    val summary: String
        get() = data.summary

    @get:JsonIgnore
    val subType: ImageSubType
        get() = data.subType
}
