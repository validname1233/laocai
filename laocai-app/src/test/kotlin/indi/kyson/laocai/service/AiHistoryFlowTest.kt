package indi.kyson.laocai.service

import indi.kyson.laocai.ai.ChatClientFactory
import indi.kyson.laocai.ai.ChatHistoryRenderer
import indi.kyson.laocai.ai.model.ChatHistory
import indi.kyson.laocai.bot.Bot
import indi.kyson.laocai.bot.MilkyEventSource
import indi.kyson.laocai.bot.autoconfigure.LaocaiBotAutoConfiguration
import indi.kyson.laocai.bot.entity.GroupEntity
import indi.kyson.laocai.bot.entity.GroupMemberEntity
import indi.kyson.laocai.bot.enums.ImageSubType
import indi.kyson.laocai.bot.enums.Role
import indi.kyson.laocai.bot.enums.Sex
import indi.kyson.laocai.bot.event.Event
import indi.kyson.laocai.bot.event.GroupMessageEvent
import indi.kyson.laocai.bot.listener.EventDispatcher
import indi.kyson.laocai.bot.segment.*
import indi.kyson.laocai.handler.AiHandler
import indi.kyson.laocai.handler.ChatHistoryHandler
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import java.nio.file.Files
import java.time.Duration
import org.mockito.ArgumentMatchers.*
import org.mockito.Mockito.*
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.messages.AssistantMessage
import org.springframework.ai.chat.model.ChatModel
import org.springframework.ai.chat.model.ChatResponse
import org.springframework.ai.chat.model.Generation
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.DefaultApplicationArguments
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.Base64
import java.util.concurrent.TimeoutException

class AiHistoryFlowTest {
    @TempDir lateinit var directory: Path
    @Test
    fun allMessagesAreStoredAndOnlyCurrentMentionTriggersReply() {
        val flow = Flow()
        flow.run { dispatcher ->
            dispatcher.dispatch(event(TextSegment.of("ordinary"), MentionSegment.of(8)))
            assertTrue(flow.sent.isEmpty())
            dispatcher.dispatch(event(TextSegment.of("question"), MentionSegment.of(SELF)))
            dispatcher.dispatch(event(TextSegment.of("after")))
        }
        assertEquals(listOf("answer"), flow.sent)
        assertEquals(listOf(USER, USER, SELF, USER), flow.saved.map { it.senderId })
        val input = flow.prompts.single().contents
        assertTrue(input.contains("ordinary@8（userId: 8）"))
        assertTrue(input.contains("question@9（userId: 9）"))
        assertEquals(1, Regex("question").findAll(input).count())
        assertEquals(setOf(USER, 8L, SELF), flow.resolvedIds)
    }

    @Test
    fun audioRouteRecordsInputWithoutAlsoGeneratingOrdinaryReply() {
        val flow = Flow()
        flow.run { it.dispatch(event(TextSegment.of("/audio hello"), MentionSegment.of(SELF))) }
        assertEquals(1, flow.saved.size)
        assertEquals(1, flow.audioEvents.size)
        assertTrue(flow.prompts.isEmpty())
    }

    @Test
    fun gifMessageIsIgnoredByHistoryAndAiListeners() {
        val source = directory.resolve("source.gif")
        Files.write(source, Base64.getDecoder().decode("R0lGODlhAQABAIAAAAAAAP///ywAAAAAAQABAAACAUwAOw=="))
        val image = IncomingImageSegment.Data("gif-resource", source.toUri().toString(), 1, 1, "", ImageSubType.NORMAL).toSegment()
        val flow = Flow(ImageCacheService(directory.resolve("cache")))

        flow.run { it.dispatch(event(MentionSegment.of(SELF), image)) }

        assertTrue(flow.saved.isEmpty())
        assertTrue(flow.sent.isEmpty())
        assertTrue(flow.prompts.isEmpty())
    }

    @Test
    fun replyIsRecordedOnlyAfterConfirmedSend() {
        val flow = Flow()
        flow.run { it.dispatch(event(TextSegment.of("question"), MentionSegment.of(SELF))) }
        assertEquals(listOf(USER), flow.savedAtSend.single().map { it.senderId })
        assertEquals(listOf(USER, SELF), flow.saved.map { it.senderId })
        assertTrue(flow.saved.last().time > 0)
    }

    @Test
    fun explicitSendFailureNotifiesButDoesNotSaveTheReplyOrFailurePrompt() {
        val flow = Flow().apply { sendResult = Mono.just("""{"status":"failed","retcode":1,"message":"rejected"}""") }
        flow.run { it.dispatch(event(MentionSegment.of(SELF))) }
        assertEquals(listOf("answer", "AI回复失败"), flow.sent)
        assertEquals(listOf(USER), flow.saved.map { it.senderId })
    }

    @Test
    fun unknownSendResultDoesNotCauseAnotherSendOrBotRecord() {
        listOf(Mono.error<String>(TimeoutException("unknown delivery")), Mono.empty<String>(), Mono.just("not json")).forEach { result ->
            val flow = Flow().apply { sendResult = result }
            flow.run { it.dispatch(event(MentionSegment.of(SELF))) }
            assertEquals(listOf("answer"), flow.sent)
            assertEquals(listOf(USER), flow.saved.map { it.senderId })
        }
    }

    @Test
    fun generationFailureOrBlankReplyNotifiesWithoutSavingFailure() {
        listOf(null, "  ").forEach { answer ->
            val flow = Flow().apply { this.answer = answer }
            flow.run { it.dispatch(event(MentionSegment.of(SELF))) }
            assertEquals(listOf("AI回复失败"), flow.sent)
            assertEquals(listOf(USER), flow.saved.map { it.senderId })
        }
    }

    @Test
    fun failedBotHistoryAppendDoesNotRetransmitSuccessfulReply() {
        val flow = Flow().apply { failBotAppend = true }
        flow.run { it.dispatch(event(MentionSegment.of(SELF))) }
        assertEquals(listOf("answer"), flow.sent)
        assertEquals(listOf(USER), flow.saved.map { it.senderId })
    }

    @Test
    fun submittedMediaUsesGlobalOrderAndSurvivesCacheDeletion() {
        Files.write(directory.resolve("A"), byteArrayOf(1, 2))
        Files.write(directory.resolve("B"), byteArrayOf(3, 4))
        fun image(id: String) = IncomingImageSegment.Data(id, "", 1, 1, "", indi.kyson.laocai.bot.enums.ImageSubType.NORMAL).toSegment()
        val flow = Flow(ImageCacheService(directory)).apply {
            saved += ChatHistory(0, 2, listOf(image("A"), image("missing")))
            beforeModel = { Files.delete(directory.resolve("A")); Files.delete(directory.resolve("B")) }
        }
        flow.run { it.dispatch(event(MentionSegment.of(SELF), image("B"), image("A"))) }
        assertEquals(listOf("answer"), flow.sent)
        val user = flow.prompts.single().userMessage
        assertTrue(user.text?.contains("[图片#1][图片（资源不可用）]") == true)
        assertTrue(user.text?.contains("[图片#2][图片#3]") == true)
        assertEquals(3, user.media.size)
        assertArrayEquals(byteArrayOf(1, 2), user.media[0].data as ByteArray)
        assertArrayEquals(byteArrayOf(3, 4), user.media[1].data as ByteArray)
        assertArrayEquals(byteArrayOf(1, 2), user.media[2].data as ByteArray)
    }

    @Test
    fun historyCanRecoverFromTransientWriteFailureBeforeAiReadsIt() {
        val flow = Flow().apply { failUserAppends = 1 }
        flow.run { it.dispatch(event(TextSegment.of("recovered"), MentionSegment.of(SELF))) }
        assertEquals(listOf(USER, SELF), flow.saved.map { it.senderId })
        assertTrue(flow.prompts.single().contents.contains("recovered"))
        assertEquals(listOf("answer"), flow.sent)
    }

    @Test
    fun exhaustedHistoryRetriesStaySilentWithoutMention() {
        val flow = Flow().apply { failUserAppends = Int.MAX_VALUE }
        flow.run { it.dispatch(event(TextSegment.of("ordinary"))) }
        assertTrue(flow.saved.isEmpty())
        assertTrue(flow.sent.isEmpty())
    }

    @Test
    fun failedHistoryNotifiesButDoesNotBlockLaterAiListener() {
        val flow = Flow().apply {
            failUserAppends = Int.MAX_VALUE
            saved += ChatHistory(0, 2, listOf(TextSegment.of("previous history")))
        }
        flow.run { it.dispatch(event(MentionSegment.of(SELF))) }
        assertEquals(listOf("好像出了点问题..", "answer"), flow.sent)
        assertEquals(listOf(2L, SELF), flow.saved.map { it.senderId })
        assertFalse(flow.prompts.single().contents.contains("AI回复失败"))
    }

    @Test
    fun repeatedDeliveryIsNotDeduplicated() {
        val flow = Flow()
        flow.run { dispatcher ->
            val input = event(MentionSegment.of(SELF))
            dispatcher.dispatch(input)
            dispatcher.dispatch(input)
        }
        assertEquals(listOf("answer", "answer"), flow.sent)
        assertEquals(listOf(USER, SELF, USER, SELF), flow.saved.map { it.senderId })
    }

    private class Flow(private val images: ImageCacheService = ImageCacheService()) {
        val saved = mutableListOf<ChatHistory>()
        val sent = mutableListOf<String>()
        val savedAtSend = mutableListOf<List<ChatHistory>>()
        val prompts = mutableListOf<Prompt>()
        val audioEvents = mutableListOf<GroupMessageEvent>()
        var resolvedIds = emptySet<Long>()
        var answer: String? = "answer"
        var failBotAppend = false
        var sendResult: Mono<String> = Mono.just("""{"status":"ok","retcode":0,"data":{"message_seq":1}}""")
        private val bot = mock(Bot::class.java)
        private val history = mock(ChatHistoryService::class.java)
        private val profiles = mock(UserProfileService::class.java)
        private val audio = mock(AudioReplyService::class.java)
        private val model = mock(ChatModel::class.java)
        private val factory = mock(ChatClientFactory::class.java)
        var failUserAppends = 0
        var beforeModel: () -> Unit = {}

        init {
            doReturn(org.springframework.ai.chat.prompt.ChatOptions.builder().build()).`when`(model).options
            doAnswer { invocation ->
                val history = invocation.getArgument<ChatHistory>(1)
                if (history.senderId == USER && failUserAppends-- > 0) error("Redis unavailable")
                if (history.senderId == SELF && failBotAppend) error("Redis unavailable")
                saved += history
                null
            }.`when`(history).append(anyLong(), any(ChatHistory::class.java) ?: ChatHistory(0, 0, emptyList()))
            doAnswer { saved.toList() }.`when`(history).load(anyLong())
            doAnswer { invocation ->
                resolvedIds = invocation.getArgument<Collection<Long>>(1).toSet()
                emptyMap<Long, String>()
            }.`when`(profiles).resolve(anyLong(), anyCollection<Long>())
            doAnswer { invocation ->
                sent += invocation.getArgument<List<Segment>>(1).filterIsInstance<TextSegment>().joinToString("") { it.text }
                savedAtSend += saved.toList()
                sendResult
            }.`when`(bot).sendGroupMsg(anyLong(), anyList<Segment>())
            doAnswer { invocation ->
                beforeModel()
                prompts += invocation.getArgument<Prompt>(0)
                val text = answer ?: error("generation failed")
                ChatResponse(listOf(Generation(AssistantMessage(text))))
            }.`when`(model).call(any(Prompt::class.java))
            doReturn(ChatClient.builder(model).build()).`when`(factory).getChatLaocaiClient(anyLong())
            doAnswer { invocation -> audioEvents += invocation.getArgument<GroupMessageEvent>(0); null }
                .`when`(audio).handle(any(GroupMessageEvent::class.java) ?: event())
        }

        fun run(test: (EventDispatcher) -> Unit) {
            val source = mock(MilkyEventSource::class.java)
            doReturn(Flux.empty<Event>()).`when`(source).eventFlux()
            ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(LaocaiBotAutoConfiguration::class.java))
                .withPropertyValues("laocai.milky.url=http://unused.invalid")
                .withBean("milkyWebClient", WebClient::class.java, { WebClient.builder().exchangeFunction { Mono.error(AssertionError("No network allowed")) }.build() })
                .withBean(MilkyEventSource::class.java, { source })
                .withBean(Bot::class.java, { bot })
                .withBean(ChatHistoryHandler::class.java, { ChatHistoryHandler(history, images, bot) })
                // Register AI first; the actual scanner/priority ordering, not test order, must run history first.
                .withBean(AiHandler::class.java, { AiHandler(AiConversationService(factory, bot, history, images, profiles, ChatHistoryRenderer(), tools.jackson.databind.json.JsonMapper.builder().build()), audio) })
                .run { context ->
                    assertNull(context.startupFailure)
                    context.getBeansOfType(ApplicationRunner::class.java).values.forEach { it.run(DefaultApplicationArguments()) }
                    test(context.getBean(EventDispatcher::class.java))
                }
        }
    }

    companion object {
        private const val GROUP = 123L
        private const val USER = 1L
        private const val SELF = 9L
        private fun event(vararg segments: Segment) = GroupMessageEvent(
            0, SELF, GROUP, 1, USER, segments.toList(), GroupEntity(GROUP, "test", 2, 10),
            GroupMemberEntity(USER, "", Sex.UNKNOWN, GROUP, "", "", 0, Role.MEMBER, 0, 0, null),
        )
    }
}
