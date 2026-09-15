package indi.kyson.laocai.service

import indi.kyson.laocai.ai.GPTSoVITSClient
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class AiCacheCleanupService(
    private val imageCache: ImageCacheService,
    private val ttsClient: GPTSoVITSClient,
) {
    @Scheduled(cron = "0 0 3 * * *")
    fun clean() {
        ttsClient.cleanExpiredOutputs()
        imageCache.cleanExpired()
    }
}
