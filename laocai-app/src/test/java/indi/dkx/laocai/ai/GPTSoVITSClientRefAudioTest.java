package indi.dkx.laocai.ai;

import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 验证参考音频能否直接从 tmp/audios/ref 读取。
 * <p>
 * 这一步是整条语音链路的前置条件：路径拿不到，TTS 服务就无法读参考音频。
 */
class GPTSoVITSClientRefAudioTest {

    @Test
    void 应直接读取tmp目录中的参考音频() throws Exception {
        GPTSoVITSClient client = new GPTSoVITSClient(WebClient.builder());
        client.prepareRefAudio();

        Path refAudio = client.getRefAudioPath();

        assertNotNull(refAudio, "参考音频路径不应为空");
        assertTrue(refAudio.isAbsolute(), "参考音频路径必须是绝对路径");
        assertTrue(Files.exists(refAudio), "参考音频文件应真实存在: " + refAudio);
        assertTrue(Files.size(refAudio) > 0, "参考音频不应为空文件");

        System.out.println("参考音频路径: " + refAudio);
        System.out.println("文件大小: " + Files.size(refAudio) + " bytes");
    }
}
