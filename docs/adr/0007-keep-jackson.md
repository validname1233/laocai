# 序列化继续使用 Jackson，不切换到 kotlinx.serialization

`Event`/`Segment` 改为 Kotlin sealed 继承体系后，`kotlinx.serialization` 对多态 sealed class 有原生支持，理论上可以替换掉现有手写的 `ValueDeserializer`（`EventDeserializer`/`Segment.SegmentDeserializer`）。但 Spring WebFlux 的 `WebClient`/编解码器默认深度绑定 Jackson，切换需要额外接入 `kotlinx-serialization-json` 的编解码器桥接，而现有手写 Deserializer 逻辑本身并不复杂。因此保留 Jackson（`tools.jackson`），sealed 类型迁移后手写 Deserializer 按新的类型结构原样调整即可，不引入新的序列化框架。
