# 协作开发说明

本项目为软件工程课程小组作业。为了保证最终演示可运行，请所有成员遵守以下规则：

## 1. 提交前必须检查

```bash
git status
```

确认没有误提交：

- `node_modules/`
- `target/`
- `uploads/`
- 临时压缩包
- IDE 缓存目录
- 错误嵌套的项目文件夹

## 2. 推荐提交格式

```text
feat: add certificate apply page
fix: correct file upload response
chore: update git workflow scripts
docs: update API document
```

## 3. 目录不能嵌套

仓库根目录应直接包含 `README.md`、`member-c/`、`miniprogram/`、`db/`、`docs/`。不要提交 `information_school_platform-main/` 这种外层文件夹。

## 4. 数据库脚本同步

新增或修改表结构时，需要同步修改：

- `db/kingbase/`
- 对应成员模块目录中的 `db/`
- README 或接口文档中的字段说明

## 5. 成员D维护内容

成员D负责文件服务、统计报表、Git 仓库管理和前端工程规范。合并前如涉及接口路径、字段、权限、文件存储或报表，请同步通知成员D检查。
