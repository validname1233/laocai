package indi.kyson.laocai.service

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.Ticker
import indi.kyson.laocai.bot.Bot
import indi.kyson.laocai.bot.enums.ResponseStatus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Duration
import java.util.concurrent.TimeUnit

/** Resolves group-scoped display names with a short-lived, best-effort cache. */
@Service
class UserProfileService {
    private val cache: Cache<ProfileKey, String>
    private val bot: Bot
    private val ticker: Ticker
    private val lookupTimeout: Duration
    private val maxConcurrentLookups: Int

    @Autowired
    constructor(bot: Bot) : this(bot, Ticker.systemTicker(), DEFAULT_LOOKUP_TIMEOUT, DEFAULT_MAX_CONCURRENT_LOOKUPS)

    constructor(
        bot: Bot,
        ticker: Ticker,
        lookupTimeout: Duration = DEFAULT_LOOKUP_TIMEOUT,
        maxConcurrentLookups: Int = DEFAULT_MAX_CONCURRENT_LOOKUPS,
    ) {
        require(!lookupTimeout.isNegative && !lookupTimeout.isZero) { "lookupTimeout must be positive" }
        require(maxConcurrentLookups > 0) { "maxConcurrentLookups must be positive" }
        this.bot = bot
        this.ticker = ticker
        this.lookupTimeout = lookupTimeout
        this.maxConcurrentLookups = maxConcurrentLookups
        this.cache = Caffeine.newBuilder()
            .maximumSize(MAX_CACHE_SIZE)
            .expireAfterWrite(CACHE_TTL.toMinutes(), TimeUnit.MINUTES)
            .ticker(ticker)
            .build()
    }

    fun resolve(groupId: Long, userIds: Collection<Long>): Map<Long, String> {
        if (userIds.isEmpty()) return emptyMap()
        return Flux.fromIterable(userIds.distinct())
            .flatMap({ userId -> resolveOne(groupId, userId) }, maxConcurrentLookups)
            .collectMap({ it.first }, { it.second })
            .onErrorReturn(emptyMap())
            .block() ?: emptyMap()
    }

    private fun resolveOne(groupId: Long, userId: Long): Mono<Pair<Long, String>> {
        val key = ProfileKey(groupId, userId)
        cache.getIfPresent(key)?.let { return Mono.just(userId to it) }
        return Mono.defer { bot.getGroupMemberInfo(groupId, userId, false) }
            .timeout(lookupTimeout)
            .filter { it.status == ResponseStatus.OK }
            .mapNotNull { it.data?.member }
            .mapNotNull { member -> member.card.trim().ifEmpty { member.nickname.trim().ifEmpty { null } } }
            .doOnNext { cache.put(key, it) }
            .map { userId to it }
            .onErrorResume { Mono.empty() }
    }

    private data class ProfileKey(val groupId: Long, val userId: Long)

    companion object {
        private val CACHE_TTL = Duration.ofMinutes(5)
        private val DEFAULT_LOOKUP_TIMEOUT = Duration.ofSeconds(1)
        private const val DEFAULT_MAX_CONCURRENT_LOOKUPS = 8
        private const val MAX_CACHE_SIZE = 10_000L
    }
}
