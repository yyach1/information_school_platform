# 成员 D 提交说明

本次只完成组长要求的第 1、2 项：文件存储服务、统计分析模块。

## 已完成内容

### 1. 文件存储服务

后端位置：

- `member-c/src/main/java/com/ischool/isp/controller/FileController.java`
- `member-c/src/main/java/com/ischool/isp/service/FileStorageService.java`
- `member-c/src/main/java/com/ischool/isp/service/impl/FileStorageServiceImpl.java`
- `member-c/src/main/java/com/ischool/isp/entity/FileRecord.java`
- `member-c/src/main/java/com/ischool/isp/mapper/FileRecordMapper.java`

接口：

- `POST /api/files/upload`
- `GET /api/files`
- `GET /api/files/{fileId}/meta`
- `GET /api/files/{fileId}`
- `GET /api/files/{fileId}/preview`
- `DELETE /api/files/{fileId}`

实现点：

- 本地 `uploads/` 目录存储文件；
- `file_record` 表保存文件元数据；
- 学生只能访问自己的文件，教师/管理员可访问全部；
- 支持下载、预览、逻辑删除；
- 支持 A/B 模块通过 `relatedType`、`relatedId` 关联材料或证明申请。

### 2. 统计分析模块

后端位置：

- `member-c/src/main/java/com/ischool/isp/controller/ReportController.java`
- `member-c/src/main/java/com/ischool/isp/service/ReportService.java`
- `member-c/src/main/java/com/ischool/isp/service/impl/ReportServiceImpl.java`
- `member-c/src/main/java/com/ischool/isp/mapper/ReportMapper.java`
- `member-c/src/main/java/com/ischool/isp/dto/response/ReportOverviewResponse.java`
- `member-c/src/main/java/com/ischool/isp/dto/response/ReportCountItem.java`
- `member-c/src/main/java/com/ischool/isp/dto/response/UploadTrendItem.java`

接口：

- `GET /api/reports/overview`
- `GET /api/reports/material-status`
- `GET /api/reports/process-status`
- `GET /api/reports/certificate-status`
- `GET /api/reports/upload-trend?days=7`
- `GET /api/reports/export?type=overview`

管理端页面：

- `member-c/admin-web/src/views/ReportAnalysis.vue`
- `member-c/admin-web/src/api/report.ts`
- 已接入左侧菜单和路由：`/reports`

### 3. 数据库脚本和文档

- `db/kingbase/004_member_d_file_report.sql`
- `member-c/db/004_member_d_file_report.sql`
- `docs/成员D_文件统计模块_接口说明书.md`

## 运行前需要执行的 SQL

先执行 C 的用户核心表，再执行 D 的脚本：

```bash
psql -f db/kingbase/001_init_user_core.sql
psql -f db/kingbase/004_member_d_file_report.sql
```

如果使用 Kingbase，把 `psql` 换成对应的 Kingbase 命令行工具即可。

## 联调提醒

- A 组提交材料时调用 `POST /api/files/upload`，建议传 `relatedType=MATERIAL`。
- B 组电子证明附件也复用该上传接口，建议传 `relatedType=CERTIFICATE`。
- 报表接口依赖 `material`、`student_process`、`certificate_application` 三类业务表；如果 A/B 已有正式表，D 脚本的 `CREATE TABLE IF NOT EXISTS` 不会覆盖。

## 建议提交信息

```bash
git add .
git commit -m "feat: implement member D file storage and report module"
git push origin main
```
