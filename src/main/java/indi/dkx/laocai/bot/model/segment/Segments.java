package indi.dkx.laocai.bot.model.segment;

import indi.dkx.laocai.bot.model.enums.ImageSubType;
import indi.dkx.laocai.bot.model.segment.data.*;

/**
 * 消息段工厂。
 * <p>
 * 上层只需要面向语义创建消息段，不需要了解每种 segment 的具体构造细节。
 */
public class Segments {
    public static Segment text(String text){
        return create("text", new TextSegmentData(text));
    }
    public static Segment message(Long messageSeq){
        return create("shouldReply", new OutgoingReplySegmentData(messageSeq));
    }
    public static Segment face(String faceId, boolean isLarge){
        return create("face", new FaceSegmentData(faceId, isLarge));
    }
    public static Segment mention(Long user_id){
        return create("mention", new MentionSegmentData(user_id));
    }
    public static Segment image(String uri, ImageSubType subType, String summary){
        return create("image", new OutgoingImageSegmentData(uri, subType, summary));
    }
    private static Segment create(String type, SegmentData data){
        return new Segment(type, data);
    }
}


