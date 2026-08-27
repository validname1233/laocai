package indi.kyson.laocai.handler

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue

class AudioFilterRegexTest {

    private val AUDIO = Regex("(?s)/audio\\b.*")
    private val NOT_AUDIO = Regex("(?s)(?!/audio\\b).*")

    @Test
    fun 应匹配的audio命令形态() {
        assertTrue(AUDIO.matches("/audio 你好"), "空格分隔")
        assertTrue(AUDIO.matches("/audio你好"), "中文紧跟，中文不是单词字符所以存在边界")
        assertTrue(AUDIO.matches("/audio"), "只有命令本身")
        assertTrue(AUDIO.matches("/audio 第一行\n第二行"), "跨行内容需要 (?s)")
    }

    @Test
    fun 不应匹配的形态() {
        assertFalse(AUDIO.matches("/audiofoo"), "单词未结束，不算命令")
        assertFalse(AUDIO.matches("你好 /audio"), "命令不在开头")
        assertFalse(AUDIO.matches("audio 你好"), "缺少斜杠")
        assertFalse(AUDIO.matches(""), "空文本")
    }

    @Test
    fun 群聊过滤应排除audio命令但放过其他消息() {
        assertFalse(NOT_AUDIO.matches("/audio 你好"), "命令消息不该进群聊闲聊")
        assertFalse(NOT_AUDIO.matches("/audio"), "裸命令同样要排除")

        assertTrue(NOT_AUDIO.matches("今天天气不错"), "普通消息要放过")
        assertTrue(NOT_AUDIO.matches(""), "纯图片消息 plainText 为空，必须放过")
        assertTrue(NOT_AUDIO.matches("/audiofoo"), "不是命令，按普通消息处理")
        assertTrue(NOT_AUDIO.matches("多行\n消息"), "跨行普通消息要放过")
    }
}

