package indi.kyson.laocai.bot.annotation;

/**
 * Filter 关键词的匹配方式。
 * <p>
 * 不同业务场景对"匹配"的定义不一样，单一正则强迫简单场景也要写正则，不够贴近书写习惯。
 */
public enum MatchType {
    /** 全等 */
    EQUALS,
    /** 全等，忽略大小写 */
    EQUALS_IGNORE_CASE,
    /** 前缀匹配 */
    STARTS_WITH,
    /** 后缀匹配 */
    ENDS_WITH,
    /** 包含匹配 */
    CONTAINS,
    /** 正则完全匹配，等价于 String.matches */
    REGEX,
    /** 正则包含匹配，文本中存在满足正则的子串即可 */
    REGEX_CONTAINS
}
