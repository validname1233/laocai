package indi.dkx.laocai.bot.model.response.data;

import indi.dkx.laocai.bot.model.enums.Sex;

/**
 * 用户资料快照。
 * <p>
 * Bot 处理群消息时只需要一组可展示的个人信息，不需要保留整个原始响应对象。
 * @param nickname 用户昵称
 * @param qid 用户 QID
 * @param age 年龄
 * @param sex 性别
 * @param remark 备注
 * @param bio 个人签名
 * @param level 等级
 * @param country 国家
 * @param city 城市
 * @param school 学校
 */
public record UserProfile(
        String nickname,
        String qid,
        Integer age,
        Sex sex,
        String remark,
        String bio,
        Integer level,
        String country,
        String city,
        String school
) {
}


