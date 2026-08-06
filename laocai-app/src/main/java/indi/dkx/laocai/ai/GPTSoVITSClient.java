package indi.dkx.laocai.ai;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * GPT-SoVITS 语音合成客户端。
 * <p>
 * 合成参数、参考音频和落盘策略是一组强耦合的细节，收敛到一个组件里，业务侧只需要给文本、拿文件。
 * 对应的服务由 GPT-SoVITS 的 api_v2.py 启动，成功时返回 wav 二进制，失败时返回 4xx + JSON。
 */
@Slf4j
@Component
public class GPTSoVITSClient {

    /**
     * TTS 服务地址。
     */
    private static final String TTS_URL = "http://127.0.0.1:9880/tts";

    /**
     * 参考音频文件名。
     */
    private static final String REF_AUDIO_FILENAME = "私が教えられる範囲でお教えしますのでいつでもお声掛けください時間も空きましたし.wav";

    /**
     * 参考音频对应的提示文本，必须和音频内容一致，否则音色迁移效果会变差。
     */
    private static final String PROMPT_TEXT = "私が教えられる範囲でお教えしますのでいつでもお声掛けください時間も空きましたし";

    /**
     * 参考音频目录。
     */
    private static final Path REF_AUDIO_DIR = Paths.get("tmp/audios/ref");

    /**
     * 合成结果输出目录。
     */
    private static final Path OUTPUT_DIR = Paths.get("tmp/audios");

    /**
     * 合成音频保留天数。
     */
    private static final long OUTPUT_TTL_DAYS = 2;

    /**
     * 单次合成超时时间。
     * <p>
     * GPT-SoVITS 是本地推理，一句话常见几秒到十几秒，超时要给够，否则会误判失败。
     */
    private static final Duration TIMEOUT = Duration.ofSeconds(120);

    private final WebClient ttsWebClient;

    /**
     * 参考音频在本地文件系统的绝对路径。
     * <p>
     * TTS 服务是独立进程，自己去磁盘读这个文件，所以必须是真实存在的绝对路径，不能是 classpath 内的资源。
     */
    private Path refAudioPath;

    public GPTSoVITSClient(WebClient.Builder webClientBuilder) {
        // wav 可能有几百 KB 到几 MB，默认 256KB 的内存缓冲不够，需要显式放大
        this.ttsWebClient = webClientBuilder
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(32 * 1024 * 1024))
                .build();
    }

    /**
     * 启动时检查参考音频。
     * <p>
     * 参考音频由部署环境直接放在 tmp/audios/ref 下，TTS 服务是独立进程，因此这里使用真实文件的绝对路径。
     */
    @PostConstruct
    void prepareRefAudio() {
        Path target = REF_AUDIO_DIR.resolve(REF_AUDIO_FILENAME).toAbsolutePath().normalize();
        if (!Files.isRegularFile(target)) {
            refAudioPath = null;
            log.error("参考音频不存在，语音合成将不可用。请将文件放到: {}", target);
            return;
        }

        refAudioPath = target;
        log.info("使用参考音频: {}", refAudioPath);
    }

    /**
     * 合成语音并落盘。
     * <p>
     * 返回本地 wav 路径，让调用方自己决定怎么发送；失败时返回空 Mono，由调用方决定降级方式。
     * @param text 要朗读的日语文本
     * @return 合成后的 wav 文件路径
     */
    public Mono<Path> synthesize(String text) {
        if (refAudioPath == null) {
            log.error("参考音频不可用，跳过语音合成");
            return Mono.empty();
        }

        // 用 LinkedHashMap 而不是 Map.of，既保留字段顺序方便对照日志，也允许后续加可空字段
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("text", text);
        payload.put("text_lang", "ja");
        payload.put("ref_audio_path", refAudioPath.toString());
        payload.put("prompt_text", PROMPT_TEXT);
        payload.put("prompt_lang", "ja");
        payload.put("text_split_method", "cut5");
        payload.put("media_type", "wav");
        payload.put("streaming_mode", false);

        return ttsWebClient.post()
                .uri(TTS_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .retrieve()
                // 失败时服务端返回的是 JSON 错误体，必须在这里拦下来，否则会把错误信息当 wav 写进文件
                .onStatus(HttpStatusCode::isError, response -> response
                        .bodyToMono(String.class)
                        .defaultIfEmpty("<empty>")
                        .flatMap(body -> Mono.error(new IllegalStateException(
                                "TTS 合成失败: status=%s body=%s".formatted(response.statusCode(), body)))))
                .bodyToMono(byte[].class)
                .timeout(TIMEOUT)
                .map(this::writeWav)
                .doOnError(e -> log.error("语音合成失败: text={}", abbreviate(text), e))
                .onErrorResume(_ -> Mono.empty());
    }

    /**
     * 把合成结果写入 tmp 目录。
     * <p>
     * 文件名用随机值，避免并发请求互相覆盖；协议侧只认本地路径，所以要返回落盘后的位置。
     */
    private Path writeWav(byte[] audio) {
        try {
            Files.createDirectories(OUTPUT_DIR);
            Path output = OUTPUT_DIR.resolve("tts-%s.wav".formatted(UUID.randomUUID()));
            Files.write(output, audio);
            log.info("语音合成完成: {} ({} bytes)", output, audio.length);
            return output.toAbsolutePath();
        } catch (IOException e) {
            throw new UncheckedIOException("写入合成音频失败", e);
        }
    }

    /**
     * 清理过期的合成音频。
     * <p>
     * 每次请求都会新落一个 wav，不清理就会一直堆在磁盘上；参考音频目录要跳过，它是长期依赖。
     */
    public void cleanExpiredOutputs() {
        if (!Files.isDirectory(OUTPUT_DIR)) {
            return;
        }
        Instant threshold = Instant.now().minus(OUTPUT_TTL_DAYS, ChronoUnit.DAYS);
        try (var stream = Files.list(OUTPUT_DIR)) {
            int[] deleted = {0};
            stream.filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().startsWith("tts-"))
                    .forEach(p -> {
                        try {
                            if (Files.getLastModifiedTime(p).toInstant().isBefore(threshold)) {
                                Files.delete(p);
                                deleted[0]++;
                            }
                        } catch (Exception e) {
                            log.warn("删除过期语音失败: {}", p, e);
                        }
                    });
            if (deleted[0] > 0) {
                log.info("清理语音缓存: 删除 {} 个过期文件", deleted[0]);
            }
        } catch (Exception e) {
            log.warn("扫描语音目录失败", e);
        }
    }

    /**
     * 截断过长文本，只用于日志。
     */
    private String abbreviate(String text) {
        return text.length() <= 40 ? text : text.substring(0, 40) + "...";
    }

    /**
     * 供日志和测试查看当前使用的参考音频路径。
     */
    public Path getRefAudioPath() {
        return refAudioPath;
    }
}
