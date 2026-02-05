package indi.dkx.laocai.bot.model.segment;

import indi.dkx.laocai.bot.model.segment.data.*;

//Segment构造工厂
//使用匿名子类实例化Segment抽象类，避免定义多种子类
public class Segments {
    public static Segment text(String text){
        return create("text", new TextSegmentData(text));
    }
    public static Segment message(Long messageSeq){
        return create("reply", new ReplySegmentData(messageSeq));
    }
    public static Segment face(String faceId, boolean isLarge){
        return create("face", new FaceSegmentData(faceId, isLarge));
    }
    public static Segment mention(Long user_id){
        return create("mention", new MentionSegmentData(user_id));
    }
    private static Segment create(String type, SegmentData data){
        return new Segment(type, data);
    }
}
