package indi.kyson.laocai.app.profile

import com.github.benmanes.caffeine.cache.Ticker
import indi.kyson.laocai.bot.Bot
import indi.kyson.laocai.bot.entity.GroupMemberEntity
import indi.kyson.laocai.bot.enums.*
import indi.kyson.laocai.bot.response.GroupMemberInfo
import indi.kyson.laocai.bot.response.Response
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.*
import org.mockito.Mockito.*
import reactor.core.publisher.Mono
import java.time.Duration
import java.util.concurrent.TimeoutException
import java.util.concurrent.atomic.AtomicLong

class UserProfileServiceTest {
    private val bot = mock(Bot::class.java)
    private val now = AtomicLong()
    private val service = UserProfileService(bot, { now.get() })

    @Test
    fun cardWinsThenNicknameAndMissingProfilesAreOmitted() {
        doAnswer { invocation ->
            when (val id = invocation.getArgument<Long>(1)) {
                1L -> profile(id, " card ", "nickname")
                2L -> profile(id, " \t ", " nickname ")
                3L -> profile(id, "", "  ")
                4L -> Mono.error(TimeoutException("lookup timed out"))
                else -> Mono.just(Response<GroupMemberInfo>(ResponseStatus.FAILED, 1, null, "unavailable"))
            }
        }.`when`(bot).getGroupMemberInfo(anyLong(), anyLong(), eq(false))
        assertEquals(mapOf(1L to "card", 2L to "nickname"), service.resolve(10, listOf(1, 2, 3, 4, 5)))
    }

    @Test
    fun cacheIsGroupScopedAndRefreshesAfterFiveMinutes() {
        var suffix = "before"
        doAnswer { invocation ->
            val group = invocation.getArgument<Long>(0)
            profile(1, "$group-$suffix", "nickname")
        }.`when`(bot).getGroupMemberInfo(anyLong(), anyLong(), eq(false))
        assertEquals(mapOf(1L to "10-before"), service.resolve(10, listOf(1)))
        assertEquals(mapOf(1L to "20-before"), service.resolve(20, listOf(1)))
        suffix = "after"
        now.set(Duration.ofMinutes(5).minusNanos(1).toNanos())
        assertEquals(mapOf(1L to "10-before"), service.resolve(10, listOf(1)))
        now.incrementAndGet()
        assertEquals(mapOf(1L to "10-after"), service.resolve(10, listOf(1)))
        assertEquals(mapOf(1L to "20-after"), service.resolve(20, listOf(1)))
    }

    @Test
    fun completelyFailedLookupReturnsEmptyAndCanRecover() {
        doAnswer { throw IllegalStateException("QQ unavailable") }.`when`(bot).getGroupMemberInfo(anyLong(), anyLong(), eq(false))
        assertEquals(emptyMap<Long, String>(), service.resolve(10, listOf(1, 2)))
        assertEquals(emptyMap<Long, String>(), service.resolve(10, emptyList()))
        doReturn(profile(1, "recovered", "")).`when`(bot).getGroupMemberInfo(anyLong(), anyLong(), eq(false))
        assertEquals(mapOf(1L to "recovered"), service.resolve(10, listOf(1)))
    }

    private fun profile(userId: Long, card: String, nickname: String): Mono<Response<GroupMemberInfo>> = Mono.just(
        Response(ResponseStatus.OK, 0, GroupMemberInfo(GroupMemberEntity(
            userId, nickname, Sex.UNKNOWN, 10, card, "", 0, Role.MEMBER, 0, 0, null,
        )), null),
    )
}
