package indi.kyson.laocai.bot.segment

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import indi.kyson.laocai.bot.enums.ImageSubType

/**
 * 出站的图片消息段。
 *
 * 发送时通过 uri 指明图片来源，和入站的临时资源结构不同，协议 type 都是 image。
 */
data class OutgoingImageSegment private constructor(override val data: Data) : Segment {

    data class Data(
        val uri: String,
        @JsonProperty("sub_type") val subType: ImageSubType,
        val summary: String?,
    ) {
        fun toSegment(): OutgoingImageSegment = OutgoingImageSegment(this)
    }

    override val type: String
        get() = "image"

    @get:JsonIgnore
    val uri: String
        get() = data.uri

    @get:JsonIgnore
    val subType: ImageSubType
        get() = data.subType

    @get:JsonIgnore
    val summary: String?
        get() = data.summary

    companion object {
        @JvmStatic
        fun of(uri: String, subType: ImageSubType, summary: String?): OutgoingImageSegment =
            OutgoingImageSegment(Data(uri, subType, summary))
    }
}
