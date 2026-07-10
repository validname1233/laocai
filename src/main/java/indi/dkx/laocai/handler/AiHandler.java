package indi.dkx.laocai.handler;

import indi.dkx.laocai.ai.ChatClientFactory;
import indi.dkx.laocai.ai.model.ReplyDecision;
import indi.dkx.laocai.bot.annotation.Listener;
import indi.dkx.laocai.bot.core.BotSender;
import indi.dkx.laocai.bot.model.event.Event;
import indi.dkx.laocai.bot.model.event.data.IncomingGroupMessage;
import indi.dkx.laocai.bot.model.event.data.IncomingMessage;
import indi.dkx.laocai.bot.model.response.Response;
import indi.dkx.laocai.bot.model.response.data.UserProfile;
import indi.dkx.laocai.bot.model.segment.Segments;
import indi.dkx.laocai.bot.model.segment.data.IncomingImageSegmentData;
import indi.dkx.laocai.ai.model.ChatRecord;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.content.Media;
import org.springframework.core.io.FileSystemResource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;
import redis.clients.jedis.RedisClient;
import tools.jackson.databind.json.JsonMapper;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 群聊 AI 处理器。
 * <p>
 * 把图片缓存、历史拼装、决策判断和回复生成放在同一个处理链里，才能保持上下文一致。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AiHandler {

    private static final int MESSAGE_WINDOW_SIZE = 50;

    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());

    private static final Path IMAGE_CACHE_DIR = Paths.get("tmp/images");

    private static final long IMAGE_CACHE_TTL_DAYS = 2;

    private final ChatClientFactory chatClientFactory;

    private final BotSender botSender;

    private final RedisClient redisClient;

    private final JsonMapper jsonMapper;


    @Listener
    public void test(Event<IncomingGroupMessage> event) {
        IncomingGroupMessage message = event.data();
        Long groupId = message.getGroup().groupId();
        String content = message.getPlainText();

        Response<UserProfile> response = botSender.getUserProfile(event.selfId()).block();
        assert response != null;
        String nickname = response.data().nickname();

        log.info("收到群消息: {}", content);

        // 先缓存图片，再把 resourceId 写进历史，避免 Redis 保存大块二进制或临时 URL
        List<String> imageIds = cacheImages(message);

        appendMessage(groupId, new ChatRecord(
                event.time(),
                message.getSenderId(),
                content,
                imageIds
        ));

        // 先取完整历史，再统一拼成 prompt，避免上下文在多处重复组装
        List<ChatRecord> messages = redisClient.lrange("event:incoming-group-message:%s".formatted(groupId), 0, -1)
                .stream()
                .map(json -> jsonMapper.readValue(json, ChatRecord.class))
                .toList();

        
        String historyText = messages.stream()
                .map(record -> {
                    String imgPart = record.imageIds().isEmpty()
                            ? ""
                            : " " + "[图片]".repeat(record.imageIds().size());
                    return "[%s] %s: %s%s".formatted(
                            TIME_FORMATTER.format(Instant.ofEpochSecond(record.time())),
                            record.senderId(),
                            record.content().replace('\n', ' '),
                            imgPart
                    );
                })
                .collect(Collectors.joining("\n"));

        // 按消息顺序把本地缓存的图片重建成 Media，保证占位符顺序和上下文顺序一致
        Media[] images = messages.stream()
                .flatMap(record -> record.imageIds().stream())
                .map(resourceId -> {
                    Path path = IMAGE_CACHE_DIR.resolve(resourceId);
                    return Files.exists(path) ? path : null;
                })
                .filter(Objects::nonNull)
                .map(path -> Media.builder()
                        .mimeType(detectMime(path))
                        .data(new FileSystemResource(path))
                        .build())
                .toArray(Media[]::new);

        // 先做是否回复的判断，避免不必要的内容生成调用
        ReplyDecision decision = chatClientFactory.getReplyDecisionClient(groupId)
                .prompt()
                .user(promptUserSpec -> promptUserSpec
                        .text("""
                                下面是 QQ 群最新 50 条消息，你的QQ号是%s, 昵称是%s，请判断是否需要作为群友发言

                                消息格式：
                                [发送时间] QQ号: 内容文字 [图片][图片]...

                                %s

                                文中每出现一个 [图片] 占位符就对应一张按顺序上传的图片""".formatted(event.selfId(), nickname, historyText))
                        .media(images))
                .call()
                .entity(ReplyDecision.class);

        // 决策为空时，直接停掉，避免用不完整状态继续生成回复
        if (decision == null) {
            log.error("AI的决定为空");
            return;
        }

        // 不回复就立即返回，避免再次调用人格模型浪费一次推理
        if (!decision.shouldReply()) {
            log.info("AI 决定不回消息");
            return;
        }

        // 只有允许发言时才生成具体回复，减少无效生成调用
        String aiResponse = chatClientFactory.getChatPersonaClient(groupId)
                .prompt()
                .user(promptUserSpec -> promptUserSpec
                        .text("""
                                下面是 QQ 群最新 50 条消息。你已经被允许发言，你的QQ号是%s, 昵称是%s，请根据群聊气氛生成一句要发送到群里的回复。

                                消息格式：
                                [发送时间] QQ号: 内容文字 [图片][图片]...

                                %s

                                文中每出现一个 [图片] 占位符就对应一张按顺序上传的图片""".formatted(event.selfId(), nickname, historyText))
                        .media(images))
                .call()
                .content();

        if (aiResponse == null || aiResponse.isBlank()) {
            return;
        }

        aiResponse = aiResponse.trim();

        botSender.sendGroupMsg(groupId, List.of(Segments.text(aiResponse))).block();

        appendMessage(groupId, new ChatRecord(
                Instant.now().getEpochSecond(),
                event.selfId(),
                aiResponse,
                List.of()
        ));
    }

    /**
     * 将消息追加到 Redis 列表中。
     * <p>
     * 历史消息要保持顺序和窗口长度，列表比散落的 KV 更适合这种追加式存储。
     * @param groupId 群号
     * @param record 聊天记录
     */
    private void appendMessage(Long groupId, ChatRecord record) {
        String key = "event:incoming-group-message:%s".formatted(groupId);
        String json = jsonMapper.writeValueAsString(record);

        try (var transaction = redisClient.multi()) {
            transaction.rpush(key, json);
            transaction.ltrim(key, -MESSAGE_WINDOW_SIZE, -1);
            transaction.exec();
        }
    }

    /**
     * 提取并缓存图片资源。
     * <p>
     * AI 侧需要稳定可复用的本地资源，而不是只在一次请求中有效的临时 URL。
     * 返回成功缓存的 resourceId 列表。
     */
    private List<String> cacheImages(IncomingMessage message) {
        return message.getSegments().stream()
                .filter(segment -> "image".equals(segment.getType()))
                .map(segment -> segment.getData() instanceof IncomingImageSegmentData data ? data : null)
                .filter(Objects::nonNull)
                // 单张图片只要能落到本地，就把可复用的 resourceId 留下来；失败则跳过，不阻断整条消息。
                .map(data -> {
                    String resourceId = data.resourceId();
                    String url = data.tempUrl();
                    if (resourceId == null || resourceId.isBlank() || url == null || url.isBlank()) {
                        return null;
                    }
                    try {
                        Files.createDirectories(IMAGE_CACHE_DIR);
                        Path filePath = IMAGE_CACHE_DIR.resolve(resourceId);
                        if (!Files.exists(filePath)) {
                            try (InputStream in = URI.create(url).toURL().openStream()) {
                                Files.copy(in, filePath);
                            }
                            log.info("缓存图片: rid={} -> {}", resourceId, filePath);
                        }
                        return resourceId;
                    } catch (Exception e) {
                        log.warn("下载图片失败: rid={} url={}", resourceId, url, e);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .toList();
    }

    /**
     * 推断本地图片的 MIME 类型。
     * <p>
     * 后续封装 Media 时需要尽量提供真实类型，识别失败再回退默认值。
     */
    private MimeType detectMime(Path path) {
        try (InputStream in = new BufferedInputStream(Files.newInputStream(path))) {
            String contentType = URLConnection.guessContentTypeFromStream(in);
            if (contentType != null) {
                return MimeType.valueOf(contentType);
            }
        } catch (IOException e) {
            log.warn("识别图片 MIME 失败: {}", path, e);
        }
        return MimeTypeUtils.IMAGE_JPEG;
    }

    /**
     * 定时清理过期图片缓存。
     * <p>
     * 图片缓存只服务短期会话，保留过久只会占磁盘而不会带来收益。
     */
    @Scheduled(cron = "0 0 3 * * *")
    public void cleanImageCache() {
        if (!Files.isDirectory(IMAGE_CACHE_DIR)) {
            return;
        }
        Instant threshold = Instant.now().minus(IMAGE_CACHE_TTL_DAYS, ChronoUnit.DAYS);
        try (var stream = Files.list(IMAGE_CACHE_DIR)) {
            int[] deleted = {0};
            stream.forEach(p -> {
                try {
                    if (Files.getLastModifiedTime(p).toInstant().isBefore(threshold)) {
                        Files.delete(p);
                        deleted[0]++;
                    }
                } catch (Exception e) {
                    log.warn("删除过期图片失败: {}", p, e);
                }
            });
            if (deleted[0] > 0) {
                log.info("清理图片缓存: 删除 {} 个过期文件", deleted[0]);
            }
        } catch (Exception e) {
            log.warn("扫描图片缓存目录失败", e);
        }
    }
}


