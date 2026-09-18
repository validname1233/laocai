package indi.kyson.laocai.handler

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class AudioFilterMatchTest {

    private fun isAudioMessage(text: String) = text.startsWith("/audio")

    @Test
    fun audioFilterUsesPrefixMatching() {
        assertTrue(isAudioMessage("/audio 你好"), "带参数")
        assertTrue(isAudioMessage("/audio你好"), "紧跟内容")
        assertTrue(isAudioMessage("/audio"), "只有命令本身")
        assertTrue(isAudioMessage("/audio 第一行\n第二行"), "跨行内容")
        assertTrue(isAudioMessage("/audiofoo"), "任意 /audio 前缀")
    }

    @Test
    fun nonAudioMessagesDoNotMatchThePrefix() {
        assertFalse(isAudioMessage("你好 /audio"), "命令不在开头")
        assertFalse(isAudioMessage("audio 你好"), "缺少斜杠")
        assertFalse(isAudioMessage(""), "空文本")
    }
}