package indi.kyson.laocai.service

import indi.kyson.laocai.bot.event.MessageEvent
import indi.kyson.laocai.bot.segment.IncomingImageSegment
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.util.MimeType
import org.springframework.util.MimeTypeUtils
import java.io.IOException
import java.net.URI
import java.net.URLConnection
import java.nio.file.Files
import java.nio.file.LinkOption.NOFOLLOW_LINKS
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption.REPLACE_EXISTING
import java.time.Instant
import java.time.temporal.ChronoUnit

@Service
class ImageCacheService {
    private val cacheDir: Path
    private val log = LoggerFactory.getLogger(javaClass)

    constructor() {
        cacheDir = Paths.get("tmp/images")
    }

    constructor(cacheDir: Path) {
        this.cacheDir = cacheDir
    }

    fun cache(event: MessageEvent): List<String> = event.segments.asSequence()
        .filterIsInstance<IncomingImageSegment>()
        .mapNotNull { segment ->
            val resourceId = safeResourceId(segment.resourceId) ?: return@mapNotNull null
            if (segment.tempUrl.isBlank()) return@mapNotNull null
            try {
                Files.createDirectories(cacheDir)
                if (pathOf(resourceId) == null) {
                    val connection = URI.create(segment.tempUrl).toURL().openConnection().apply {
                        connectTimeout = DOWNLOAD_TIMEOUT_MS
                        readTimeout = DOWNLOAD_TIMEOUT_MS
                    }
                    val temporary = Files.createTempFile(cacheDir, ".download-", ".tmp")
                    try {
                        connection.getInputStream().use { Files.copy(it, temporary, REPLACE_EXISTING) }
                        Files.move(temporary, cacheDir.resolve(resourceId), REPLACE_EXISTING)
                    } finally {
                        Files.deleteIfExists(temporary)
                    }
                }
                resourceId
            } catch (e: Exception) {
                log.warn("下载图片失败: rid={}", resourceId, e)
                null
            }
        }.toList()

    fun pathOf(resourceId: String): Path? = safeResourceId(resourceId)?.let { cacheDir.resolve(it) }
        ?.takeIf { Files.isRegularFile(it, NOFOLLOW_LINKS) }

    /**
     * Read before rendering so later expiry/deletion cannot desynchronise text and attached Media.
     */
    fun readAvailable(resourceIds: Collection<String>): Map<String, ByteArray> = buildMap {
        resourceIds.distinct().forEach { reference ->
            val path = pathOf(reference) ?: return@forEach
            try {
                val bytes = Files.readAllBytes(path)
                if (bytes.isNotEmpty()) put(reference, bytes)
            } catch (e: IOException) {
                log.debug("图片资源已不可读: rid={}", reference, e)
            }
        }
    }

    fun available(resourceIds: Collection<String>): Set<String> = readAvailable(resourceIds).keys

    fun detectMime(bytes: ByteArray): MimeType = bytes.inputStream().use { input ->
        URLConnection.guessContentTypeFromStream(input)?.let(MimeType::valueOf) ?: MimeTypeUtils.IMAGE_JPEG
    }

    /** A GIF cannot be sent to the configured AI model, so its whole message is ignored. */
    fun containsGif(event: MessageEvent): Boolean {
        val resourceIds = event.segments.asSequence()
            .filterIsInstance<IncomingImageSegment>()
            .map { it.resourceId }
            .toList()
        return readAvailable(resourceIds).values.any { detectMime(it) == MimeTypeUtils.IMAGE_GIF }
    }

    fun cleanExpired() {
        if (!Files.isDirectory(cacheDir)) return
        val threshold = Instant.now().minus(CACHE_TTL_DAYS, ChronoUnit.DAYS)
        try {
            Files.list(cacheDir).use { stream ->
                stream.forEach { path ->
                    try {
                        if (Files.isRegularFile(path, NOFOLLOW_LINKS) && Files.getLastModifiedTime(path).toInstant().isBefore(threshold)) {
                            Files.deleteIfExists(path)
                        }
                    } catch (e: Exception) {
                        log.warn("删除过期图片失败: {}", path, e)
                    }
                }
            }
        } catch (e: Exception) {
            log.warn("扫描图片缓存目录失败", e)
        }
    }

    private fun safeResourceId(id: String): String? {
        if (id.isBlank() || id == "." || id == ".." || id.any { it == '/' || it == '\\' || it == ':' || it == '\u0000' }) return null
        return runCatching { id.takeIf { it == Paths.get(it).fileName.toString() } }.getOrNull()
    }

    companion object {
        private const val CACHE_TTL_DAYS = 2L
        private const val DOWNLOAD_TIMEOUT_MS = 5000
    }
}
