package indi.kyson.laocai.service

import indi.kyson.laocai.bot.event.MessageEvent
import indi.kyson.laocai.bot.segment.IncomingImageSegment
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.util.MimeType
import org.springframework.util.MimeTypeUtils
import java.io.BufferedInputStream
import java.io.IOException
import java.net.URI
import java.net.URLConnection
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.time.Instant
import java.time.temporal.ChronoUnit

@Service
class ImageCacheService {
    private val log = LoggerFactory.getLogger(javaClass)

    fun cache(event: MessageEvent): List<String> = event.segments.asSequence()
        .filterIsInstance<IncomingImageSegment>()
        .mapNotNull { segment ->
            val resourceId = segment.resourceId
            val url = segment.tempUrl
            if (resourceId.isBlank() || url.isBlank()) return@mapNotNull null
            try {
                Files.createDirectories(CACHE_DIR)
                val filePath = CACHE_DIR.resolve(resourceId)
                if (!Files.exists(filePath)) {
                    URI.create(url).toURL().openStream().use { input -> Files.copy(input, filePath) }
                    log.info("缓存图片: rid={} -> {}", resourceId, filePath)
                }
                resourceId
            } catch (e: Exception) {
                log.warn("下载图片失败: rid={} url={}", resourceId, url, e)
                null
            }
        }.toList()

    fun pathOf(resourceId: String): Path? = CACHE_DIR.resolve(resourceId).takeIf { Files.exists(it) }

    fun detectMime(path: Path): MimeType = try {
        BufferedInputStream(Files.newInputStream(path)).use { input ->
            URLConnection.guessContentTypeFromStream(input)?.let(MimeType::valueOf) ?: MimeTypeUtils.IMAGE_JPEG
        }
    } catch (e: IOException) {
        log.warn("识别图片 MIME 失败: {}", path, e)
        MimeTypeUtils.IMAGE_JPEG
    }

    fun cleanExpired() {
        if (!Files.isDirectory(CACHE_DIR)) return
        val threshold = Instant.now().minus(CACHE_TTL_DAYS, ChronoUnit.DAYS)
        try {
            Files.list(CACHE_DIR).use { stream ->
                var deleted = 0
                stream.forEach { path ->
                    try {
                        if (Files.getLastModifiedTime(path).toInstant().isBefore(threshold)) {
                            Files.delete(path)
                            deleted++
                        }
                    } catch (e: Exception) { log.warn("删除过期图片失败: {}", path, e) }
                }
                if (deleted > 0) log.info("清理图片缓存: 删除 {} 个过期文件", deleted)
            }
        } catch (e: Exception) { log.warn("扫描图片缓存目录失败", e) }
    }

    companion object {
        private val CACHE_DIR: Path = Paths.get("tmp/images")
        private const val CACHE_TTL_DAYS = 2L
    }
}
