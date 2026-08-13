# Bot 框架

面向 QQ 的、Java 友好的机器人开发框架（`laocai-bot-core` + `laocai-bot-spring-boot-starter`）。负责连接 LLBot、解析事件、把事件分发给业务代码里声明的处理方法。不包含具体业务逻辑（德州扑克、AI 对话等属于消费方 `laocai-app` 的领域，不在这个上下文里）。

## Language

**Bot**:
代表一个已连接的 QQ 机器人账号，聚合身份（`selfId`）与主动发送/查询能力（发群消息、发私聊消息、发群公告、查用户资料）于一身。是消费方代码持有并调用的核心对象。
_Avoid_: BotSender（旧名，能力已并入 Bot）、Client。

**LLBot**:
框架实际连接的远端机器人网关程序。框架通过 HTTP + SSE 与它通信。
_Avoid_: 与"Milky"混用——LLBot 是程序本身，Milky 才是它对外暴露的协议。

**Milky**:
LLBot 对外暴露的通信协议（HTTP 请求 + SSE 事件流），是本框架目前唯一支持、且已知未来也只打算支持的协议。
_Avoid_: OneBot（协议风格上相似，但不是同一个标准，避免暗示完全兼容）。

**Event**:
框架从 Milky 事件流收到的一条具体事件，用 sealed 类型体系表达（而不是携带泛型 data 的信封类型）。每种事件类型对应一个具体的叶子类型。
_Avoid_: Message、Payload——"消息"只是事件携带的内容，事件本身是更外层的概念。

**MessageEvent**:
`Event` 下的一个中间层类型，代表"收到一条消息"这一类事件的公共形态（发送者、消息内容、时间等），本身不对应某一种具体场景，不能单独出现，只能是 `GroupMessageEvent` 或 `FriendMessageEvent`。
_Avoid_: IncomingMessage（旧名）。

**GroupMessageEvent** / **FriendMessageEvent**:
`MessageEvent` 的两个具体叶子类型，分别对应群消息、好友消息场景。

**Segment**:
消息内容的最小单元（纯文本、图片、@某人、表情、引用回复等），一条消息由若干 Segment 顺序组成。
_Avoid_: MessagePart、Chunk。

**Listener**:
用 `@Listener` 标注的方法，声明"我要处理哪一类 `Event`"（按方法参数的声明类型匹配，支持匹配到 `MessageEvent` 这层以同时接收两种叶子事件）。
_Avoid_: Handler——Handler 特指消费方（`laocai-app`）里承载若干 Listener 方法的业务类（如 `TexasHandler`），Handler 不是本框架定义的概念，Listener 才是。

**Filter**:
附加在 Listener 方法上的匹配条件（关键词、发送者、群号、是否 @ 机器人等），决定一次具体的 `Event` 是否命中这个 Listener。多个 Filter 之间、`MultiFilter` 内部的组合逻辑见代码注释，不在此重复。
_Avoid_: Matcher、Condition。

**MilkyEventSource**:
连接 Milky `/event` 端点、把收到的字节流解析成 `Event`、断线自动无限重试，只产出一条不会终止的 `Event` 流，不关心下游怎么消费。
_Avoid_: MilkyClient（这个类型只做入站，"客户端"太笼统，容易让人以为它也管发送——发送能力在 `Bot` 上）。

**Dispatcher**:
接收 `MilkyEventSource` 产出的 `Event` 流，按注册顺序把它交给所有匹配的 Listener 执行，并隔离单个 Listener 抛出的异常；同时负责"如何消费事件流"这层策略——背压缓冲上限、并发分发速率，跟事件从哪里来、怎么重连无关，所以这层策略挂在 Dispatcher 上而不是 MilkyEventSource 上。
