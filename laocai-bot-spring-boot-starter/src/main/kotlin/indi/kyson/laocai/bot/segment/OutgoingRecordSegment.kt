package indi.kyson.laocai.bot.segment

class OutgoingRecordSegment private constructor(override val data: Data) : Segment {

    data class Data(val uri: String)

    override val type: String
        get() = "record"

    companion object {
        @JvmStatic
        fun of(uri: String): OutgoingRecordSegment = OutgoingRecordSegment(Data(uri))
    }
}
