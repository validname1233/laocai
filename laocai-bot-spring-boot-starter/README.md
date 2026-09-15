# Laocai Bot Spring Boot Starter

面向 LLBot/Milky 的 QQ 机器人 Spring Boot Starter。

## 依赖

```kotlin
dependencies {
    implementation("indi.kyson:laocai-bot-spring-boot-starter:0.0.1-SNAPSHOT")
}
```

## 自动配置

Classpath 中存在 starter 且配置了 `laocai.milky.url` 时，Spring Boot 会自动创建 Bot 基础设施。

```yaml
laocai:
  milky:
    url: http://localhost:3010
    # 可选；为空或省略时不发送 Authorization 请求头
    access-token: your-token
  dispatcher:
    # 可选，默认 32
    concurrency: 32
    # 可选，默认 5000
    buffer-size: 5000
```

未配置 `laocai.milky.url` 时，starter 不创建 `Bot`、事件源或 Dispatcher，也不会连接 Milky 协议源。

事件流断开后固定等待 5 秒并无限重连。事件分发采用有界缓冲区，溢出时使用 `DROP_LATEST` 策略。

## 声明监听器

```java
import indi.kyson.laocai.bot.Bot;
import indi.kyson.laocai.bot.annotation.Filter;
import indi.kyson.laocai.bot.annotation.Listener;
import indi.kyson.laocai.bot.annotation.MatchType;
import indi.kyson.laocai.bot.event.GroupMessageEvent;
import indi.kyson.laocai.bot.segment.TextSegment;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PingListener {

    private final Bot bot;

    public PingListener(Bot bot) {
        this.bot = bot;
    }

    @Listener
    @Filter(value = "ping", matchType = MatchType.EQUALS)
    public void onGroupMessage(GroupMessageEvent event) {
        bot.sendGroupMsg(event.getPeerId(), List.of(TextSegment.of("pong"))).block();
    }
}
```

监听器按方法参数类型做多态匹配；参数声明为 `MessageEvent` 时可以同时接收群消息和好友消息。`Filter`、`MultiFilter` 可继续叠加文本、发送者、群号和 @ 目标条件。

## Bot API

`Bot` 是主动发送和查询能力的入口，当前提供群消息、私聊消息、群公告和用户资料查询等方法。

Spring Boot 应用通常直接注入 `Bot`。高级场景也可以脱离容器直接构造：

```java
var webClient = WebClient.builder()
        .baseUrl("http://localhost:3000/")
        .build();
var bot = new Bot(webClient);
```

直接构造只表示对象本身不依赖 `ApplicationContext`；本项目正式支持的完整启动、监听器扫描和生命周期集成仍是 Spring Boot。

## 自定义运行时 Bean

自动配置会在用户已经提供以下 Bean 时退让：

- 名为 `milkyWebClient` 的 `WebClient`
- `Bot`
- `MilkyEventSource`
- `EventDispatcher`
