# Issue tracker：GitHub

本仓库的 issue 和 spec 使用 GitHub Issues 管理。所有 issue tracker 操作都使用 `gh` CLI。

## 操作约定

- **创建 issue**：`gh issue create --title "..." --body "..."`。多行内容使用 heredoc。
- **读取 issue**：`gh issue view <number> --comments`，同时获取 labels；需要时使用 `jq` 过滤 comments 和 labels。
- **列出 issue**：`gh issue list --state open --json number,title,body,labels,comments --jq '[.[] | {number, title, body, labels: [.labels[].name], comments: [.comments[].body]}]'`，并根据需要添加 `--label` 和 `--state` 过滤条件。
- **评论 issue**：`gh issue comment <number> --body "..."`
- **添加或移除 label**：`gh issue edit <number> --add-label "..."` / `gh issue edit <number> --remove-label "..."`
- **关闭 issue**：`gh issue close <number> --comment "..."`

仓库根据 `git remote -v` 确定；在本仓库克隆目录内运行 `gh` 时，它会自动推断目标仓库。

## Pull Request 是否作为 triage 请求入口

**否。** 外部 Pull Request 不作为本仓库的 triage 请求入口。

如果未来改为是，Pull Request 可以使用与 issue 相同的 label 和状态，并使用对应的 `gh pr` 命令：

- **读取 PR**：`gh pr view <number> --comments` 和 `gh pr diff <number>`
- **列出外部 PR**：`gh pr list --state open --json number,title,body,labels,author,authorAssociation,comments`，只保留 `authorAssociation` 为 `CONTRIBUTOR`、`FIRST_TIME_CONTRIBUTOR` 或 `NONE` 的 PR，排除 `OWNER`、`MEMBER` 和 `COLLABORATOR`
- **评论、标记或关闭 PR**：使用 `gh pr comment`、`gh pr edit --add-label` / `--remove-label`、`gh pr close`

GitHub 的 issue 和 PR 共用编号空间，因此单独看到 `#42` 时，先运行 `gh pr view 42`；如果不是 PR，再运行 `gh issue view 42`。

## skill 要求“发布到 issue tracker”时

创建一个 GitHub issue。

## skill 要求“获取相关 ticket”时

运行 `gh issue view <number> --comments`。
