package indi.kyson.laocai.app.media

import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatusCode
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.io.IOException
import java.io.UncheckedIOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.LinkedHashMap
import java.util.UUID

@Component
class GPTSoVITSClient(webClientBuilder: WebClient.Builder) {

    private val log = LoggerFactory.getLogger(javaClass)
    private val ttsWebClient = webClientBuilder
        .codecs { it.defaultCodecs().maxInMemorySize(32 * 1024 * 1024) }
        .build()

    private var refAudioPathValue: Path? = null

    val refAudioPath: Path?
        get() = refAudioPathValue

    @PostConstruct
    fun prepareRefAudio() {
        val target = REF_AUDIO_DIR.resolve(REF_AUDIO_FILENAME).toAbsolutePath().normalize()
        if (!Files.isRegularFile(target)) {
            refAudioPathValue = null
            log.error("参考音频不存在，语音合成将不可用。请将文件放到: {}", target)
            return
        }

        refAudioPathValue = target
        log.info("使用参考音频: {}", refAudioPath)
    }

    fun synthesize(text: String): Mono<Path> {
        val refAudioPath = refAudioPathValue
        if (refAudioPath == null) {
            log.error("参考音频不可用，跳过语音合成")
            return Mono.empty()
        }

        val payload = LinkedHashMap<String, Any>().apply {
            put("text", text)
            put("text_lang", "ja")
            put("ref_audio_path", refAudioPath.toString())
            put("prompt_text", PROMPT_TEXT)
            put("prompt_lang", "ja")
            put("text_split_method", "cut5")
            put("media_type", "wav")
            put("streaming_mode", false)
        }

        return ttsWebClient.post()
            .uri(TTS_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(payload)
            .retrieve()
            .onStatus(HttpStatusCode::isError) { response ->
                response.bodyToMono(String::class.java)
                    .defaultIfEmpty("<empty>")
                    .flatMap { body ->
                        Mono.error(
                            IllegalStateException(
                                "TTS 合成失败: status=${response.statusCode()} body=$body",
                            ),
                        )
                    }
            }
            .bodyToMono(ByteArray::class.java)
            .timeout(TIMEOUT)
            .map(::writeWav)
            .doOnError { error -> log.error("语音合成失败: text={}", abbreviate(text), error) }
            .onErrorResume { Mono.empty() }
    }

    private fun writeWav(audio: ByteArray): Path {
        return try {
            Files.createDirectories(OUTPUT_DIR)
            val output = OUTPUT_DIR.resolve("tts-${UUID.randomUUID()}.wav")
            Files.write(output, audio)
            log.info("语音合成完成: {} ({} bytes)", output, audio.size)
            output.toAbsolutePath()
        } catch (e: IOException) {
            throw UncheckedIOException("写入合成音频失败", e)
        }
    }

    fun cleanExpiredOutputs() {
        if (!Files.isDirectory(OUTPUT_DIR)) {
            return
        }
        val threshold = Instant.now().minus(OUTPUT_TTL_DAYS, ChronoUnit.DAYS)
        try {
            Files.list(OUTPUT_DIR).use { stream ->
                var deleted = 0
                stream.filter(Files::isRegularFile)
                    .filter { it.fileName.toString().startsWith("tts-") }
                    .forEach { path ->
                        try {
                            if (Files.getLastModifiedTime(path).toInstant().isBefore(threshold)) {
                                Files.delete(path)
                                deleted++
                            }
                        } catch (e: Exception) {
                            log.warn("删除过期语音失败: {}", path, e)
                        }
                    }
                if (deleted > 0) {
                    log.info("清理语音缓存: 删除 {} 个过期文件", deleted)
                }
            }
        } catch (e: Exception) {
            log.warn("扫描语音目录失败", e)
        }
    }

    private fun abbreviate(text: String): String =
        if (text.length <= 40) text else text.substring(0, 40) + "..."

    companion object {
        private const val TTS_URL = "http://127.0.0.1:9880/tts"
        private const val REF_AUDIO_FILENAME = "私が教えられる範囲でお教えしますのでいつでもお声掛けください時間も空きましたし.wav"
        private const val PROMPT_TEXT = "私が教えられる範囲でお教えしますのでいつでもお声掛けください時間も空きましたし"
        private val REF_AUDIO_DIR: Path = Paths.get("tmp/audios/ref")
        private val OUTPUT_DIR: Path = Paths.get("tmp/audios")
        private const val OUTPUT_TTL_DAYS = 2L
        private val TIMEOUT: Duration = Duration.ofSeconds(120)
    }
}
