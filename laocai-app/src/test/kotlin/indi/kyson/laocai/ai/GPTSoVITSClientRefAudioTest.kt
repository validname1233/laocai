package indi.kyson.laocai.ai

import org.junit.jupiter.api.Test
import org.springframework.web.reactive.function.client.WebClient
import java.nio.file.Files
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue

class GPTSoVITSClientRefAudioTest {

    @Test
    fun 应直接读取tmp目录中的参考音频() {
        val client = GPTSoVITSClient(WebClient.builder())
        client.prepareRefAudio()

        val refAudio = client.refAudioPath

        assertNotNull(refAudio, "参考音频路径不应为空")
        assertTrue(refAudio!!.isAbsolute, "参考音频路径必须是绝对路径")
        assertTrue(Files.exists(refAudio), "参考音频文件应真实存在: $refAudio")
        assertTrue(Files.size(refAudio) > 0, "参考音频不应为空文件")

        println("参考音频路径: $refAudio")
        println("文件大小: ${Files.size(refAudio)} bytes")
    }
}
