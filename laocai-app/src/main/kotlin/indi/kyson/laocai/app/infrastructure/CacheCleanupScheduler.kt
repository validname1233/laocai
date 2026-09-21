package indi.kyson.laocai.app.infrastructure

import indi.kyson.laocai.app.media.GPTSoVITSClient
import indi.kyson.laocai.app.media.ImageCacheService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class CacheCleanupScheduler(
    private val imageCache: ImageCacheService,
    private val ttsClient: GPTSoVITSClient,
) {
    @Scheduled(cron = "0 0 3 * * *")
    fun clean() {
        ttsClient.cleanExpiredOutputs()
        imageCache.cleanExpired()
    }
}
