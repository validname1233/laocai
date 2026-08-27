package indi.dkx.laocai.handler;

import indi.dkx.laocai.bot.annotation.Filter;
import indi.dkx.laocai.bot.annotation.Listener;
import indi.dkx.laocai.bot.core.BotSender;
import indi.dkx.laocai.bot.model.event.Event;
import indi.dkx.laocai.bot.model.event.data.IncomingGroupMessage;
import indi.dkx.laocai.bot.model.segment.Segments;
import indi.dkx.laocai.constant.VideoConstant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class VideoHandler {

    private final BotSender botSender;

    @Listener
    @Filter("^[。\\.]dlp.*")
    public void handle(Event<IncomingGroupMessage> event) {
        IncomingGroupMessage message = event.data();
        String[] args = message.getPlainText().split(" ");
        if (args.length < 2) {
            botSender.sendGroupMsg(message.getGroup().groupId(), List.of(
                    Segments.mention(message.getSenderId()),
                    Segments.text(" 用法: .dlp <视频链接>")
            ));
            return;
        }

        String url = args[1];
        Long groupId = message.getGroup().groupId();
        Long senderId = message.getSenderId();
        log.info("要下载的视频为: {}", url);

        botSender.sendGroupMsg(groupId, List.of(
                Segments.mention(senderId),
                Segments.text(" 开始下载...")
        ));

        Thread.startVirtualThread(() -> {
            try {
                String filename = UUID.randomUUID() + ".mkv";
                ProcessBuilder pb = new ProcessBuilder(
                        "yt-dlp",
                        "-f", "bestvideo+bestaudio",
                        "--merge-output-format", "mkv",
                        "--cookies", VideoConstant.COOKIE_ROOT_DIR + "cookies.txt",
                        "-o", VideoConstant.VIDEO_DOWNLOAD_ROOT_DIR + filename,
                        url
                );
                pb.redirectErrorStream(true);
                Process process = pb.start();

                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        log.info("[yt-dlp] {}", line);
                    }
                }

                int exitCode = process.waitFor();
                String result = exitCode == 0 ? " 下载完成!" : " 下载失败，退出码: " + exitCode;
                botSender.uploadGroupFile(groupId,
                        null,
                        "file://" + VideoConstant.VIDEO_DOWNLOAD_ROOT_DIR + filename,
                        filename);
                botSender.sendGroupMsg(groupId, List.of(
                        Segments.mention(senderId),
                        Segments.text(result)
                ));
            } catch (Exception e) {
                log.error("yt-dlp 执行异常", e);
                botSender.sendGroupMsg(groupId, List.of(
                        Segments.mention(senderId),
                        Segments.text(" 下载异常: " + e.getMessage())
                ));
            }
        });
    }
}
