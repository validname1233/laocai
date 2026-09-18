package indi.kyson.laocai.bot.segment

data class OutgoingRecordSegment private constructor(override val data: Data) : Segment {

    data class Data(val uri: String) {
        fun toSegment(): OutgoingRecordSegment = OutgoingRecordSegment(this)
    }

    override val type: String
        get() = "record"

    companion object {
        @JvmStatic
        fun of(uri: String): OutgoingRecordSegment = OutgoingRecordSegment(Data(uri))
    }
}
