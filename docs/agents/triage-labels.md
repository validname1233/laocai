# Triage Labels

工程 skill 使用五个 canonical triage role。本文件将这些 role 映射到本仓库 GitHub Issues 中实际使用的 label 名称。

| mattpocock skill 中的 role | 本仓库的 label | 含义 |
| --- | --- | --- |
| `needs-triage` | `needs-triage` | 需要维护者评估该 issue |
| `needs-info` | `needs-info` | 等待 issue 提交者补充信息 |
| `ready-for-agent` | `ready-for-agent` | 需求说明完整，可以交给 Agent 实现 |
| `ready-for-human` | `ready-for-human` | 需要人工进行实现、判断或外部操作 |
| `wontfix` | `wontfix` | 不会处理该 issue |

当 skill 提到某个 triage role 时，使用表格中对应的本仓库 label。

这些 label 名称目前与 canonical role 相同。如果 GitHub 仓库中尚未创建这些 label，需要先通过 GitHub 界面或 `gh label create` 创建；本配置只记录映射关系，不会自动创建远程 label。
