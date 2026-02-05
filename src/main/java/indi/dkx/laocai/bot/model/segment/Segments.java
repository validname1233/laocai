package indi.dkx.laocai.bot.model.segment;

import indi.dkx.laocai.bot.model.segment.data.*;

//Segment构造工厂
//使用匿名子类实例化Segment抽象类，避免定义多种子类
public class Segments {
    public static Segment<TextSegmentData> text(String text){
        return create("text", new TextSegmentData(text));
    }
    public static Segment<ReplySegmentData> message(Long messageSeq){
        return create("reply", new ReplySegmentData(messageSeq));
    }
    public static Segment<FaceSegmentData> face(String faceId, boolean isLarge){
        return create("face", new FaceSegmentData(faceId, isLarge));
    }
    public static Segment<MentionSegmentData> mention(Long user_id){
        return create("mention", new MentionSegmentData(user_id));
    }
    private static <T> Segment<T> create(String type, T data){
        return new Segment<T>(type, data);
    }
}
