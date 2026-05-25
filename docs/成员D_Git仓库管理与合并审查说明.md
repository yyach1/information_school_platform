# 成员D：Git 仓库管理与合并审查说明

本文档用于统一小组仓库协作方式，减少覆盖代码、目录层级错误、换行符警告和合并冲突。

## 1. 分支策略

建议采用轻量分支模型：

| 分支 | 用途 |
|---|---|
| `main` | 可运行主线，仅合并经过检查的代码 |
| `feature/a-process` | 成员 A 流程审批模块 |
| `feature/b-miniprogram` | 成员 B 小程序与电子证明模块 |
| `feature/c-auth-log` | 成员 C 用户、权限、通知、日志模块 |
| `feature/d-file-report` | 成员 D 文件服务、统计报表、工程规范 |

个人开发时先在自己的 feature 分支提交，确认能运行后再合并到 `main`。

## 2. 推荐提交类型

| 类型 | 含义 | 示例 |
|---|---|---|
| `feat` | 新功能 | `feat: add file upload service` |
| `fix` | 修复问题 | `fix: correct report export encoding` |
| `docs` | 文档 | `docs: update member D api document` |
| `refactor` | 重构 | `refactor: split file service methods` |
| `chore` | 工程配置 | `chore: add editorconfig and git scripts` |
| `merge` | 合并分支 | `merge: feature/d-file-report into main` |

## 3. 合并前检查清单

合并前至少完成：

- `git status` 确认没有无关临时文件。
- 后端修改后执行 `cd member-c && mvn spring-boot:run`。
- 管理端修改后执行 `cd member-c/admin-web && npm run build`。
- 数据库字段变更同步到 `db/kingbase` 和模块目录。
- 涉及接口变更时同步更新 `docs/` 中的接口说明。
- 不提交 `node_modules`、`target`、`.idea`、`.vscode` 个人缓存、上传文件实体。

## 4. 目录层级要求

仓库根目录应直接包含：

```text
README.md
member-c/
miniprogram/
db/
docs/
scripts/
```

不要把整个压缩包文件夹作为子目录提交，例如不要出现：

```text
information_school_platform-main/README.md
information_school_platform-main/member-c/
```

如果出现嵌套目录，可在 PowerShell 中执行：

```powershell
robocopy ".\information_school_platform-main" "." /E /XD .git
Remove-Item -Recurse -Force ".\information_school_platform-main"
git add -A
git commit -m "fix: move files to repository root"
git push origin main
```

## 5. 成员D合并审查重点

成员 D 主要检查：

1. 是否破坏统一响应格式 `{ code, message, data, timestamp }`。
2. 是否新增 SQL 但没有写入 `db/kingbase`。
3. 是否存在前后端接口路径不一致。
4. 是否提交大文件、依赖目录或临时构建目录。
5. 是否影响 C 的认证权限配置。
6. 文件上传是否只保存路径和元数据，不把文件二进制写入数据库。

## 6. 辅助脚本

| 脚本 | 用途 |
|---|---|
| `scripts/check-project.ps1` | Windows 下检查仓库结构和环境 |
| `scripts/check-project.sh` | Linux/macOS/WSL 下检查仓库结构和环境 |
| `scripts/run-backend.ps1` | 启动 Spring Boot 后端 |
| `scripts/run-admin-web.ps1` | 启动 Vue 管理端 |
| `scripts/git-safe-merge.ps1` | 安全合并分支，先合并但不自动提交 |
| `scripts/init-git-config.ps1` | 设置换行和提交模板 |
