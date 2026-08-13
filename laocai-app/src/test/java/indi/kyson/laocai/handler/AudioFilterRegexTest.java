package indi.kyson.laocai.handler;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 校验 /audio 命令与群聊闲聊的过滤正则。
 * <p>
 * 框架用 String.matches() 做整串匹配，前缀语义完全依赖正则写法，写错会导致监听器永久不触发。
 */
class AudioFilterRegexTest {

    /**
     * handleAudio 上的过滤正则。
     */
    private static final String AUDIO = "(?s)/audio\\b.*";

    /**
     * test 上排除命令的过滤正则。
     */
    private static final String NOT_AUDIO = "(?s)(?!/audio\\b).*";

    @Test
    void 应匹配的audio命令形态() {
        assertTrue("/audio 你好".matches(AUDIO), "空格分隔");
        assertTrue("/audio你好".matches(AUDIO), "中文紧跟，中文不是单词字符所以存在边界");
        assertTrue("/audio".matches(AUDIO), "只有命令本身");
        assertTrue("/audio 第一行\n第二行".matches(AUDIO), "跨行内容需要 (?s)");
    }

    @Test
    void 不应匹配的形态() {
        assertFalse("/audiofoo".matches(AUDIO), "单词未结束，不算命令");
        assertFalse("你好 /audio".matches(AUDIO), "命令不在开头");
        assertFalse("audio 你好".matches(AUDIO), "缺少斜杠");
        assertFalse("".matches(AUDIO), "空文本");
    }

    @Test
    void 群聊过滤应排除audio命令但放过其他消息() {
        assertFalse("/audio 你好".matches(NOT_AUDIO), "命令消息不该进群聊闲聊");
        assertFalse("/audio".matches(NOT_AUDIO), "裸命令同样要排除");

        assertTrue("今天天气不错".matches(NOT_AUDIO), "普通消息要放过");
        assertTrue("".matches(NOT_AUDIO), "纯图片消息 plainText 为空，必须放过");
        assertTrue("/audiofoo".matches(NOT_AUDIO), "不是命令，按普通消息处理");
        assertTrue("多行\n消息".matches(NOT_AUDIO), "跨行普通消息要放过");
    }
}
