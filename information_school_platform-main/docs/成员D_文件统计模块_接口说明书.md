# 成员 D：文件存储与统计报表接口说明书

Base URL：`/api`  
认证方式：`Authorization: Bearer <JWT>`

## 1. 文件存储服务

### 1.1 上传文件

- 方法：`POST`
- 路径：`/files/upload`
- Content-Type：`multipart/form-data`
- 权限：登录用户

参数：

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| file | File | 是 | 上传文件 |
| ownerId | Long | 否 | 文件所属用户，不传时为当前登录用户；学生只能传本人 |
| relatedType | String | 否 | 业务类型，如 `MATERIAL`、`CERTIFICATE`、`OTHER` |
| relatedId | Long | 否 | 业务记录 id，如 `material.id` |

响应：

```json
{
  "code": 200,
  "message": "OK",
  "data": {
    "id": 1,
    "ownerId": 2,
    "relatedType": "MATERIAL",
    "relatedId": 10,
    "originalName": "入党申请书.pdf",
    "fileUrl": "/api/files/1",
    "previewUrl": "/api/files/1/preview",
    "contentType": "application/pdf",
    "fileSize": 102400,
    "status": "ACTIVE"
  }
}
```

### 1.2 文件列表

- 方法：`GET`
- 路径：`/files`
- 权限：学生只看自己的文件；教师和管理员可查询全部

查询参数：`page`、`pageSize`、`ownerId`、`relatedType`、`relatedId`、`keyword`。

### 1.3 文件元数据

- 方法：`GET`
- 路径：`/files/{fileId}/meta`

### 1.4 下载文件

- 方法：`GET`
- 路径：`/files/{fileId}`
- 返回：文件流，`Content-Disposition: attachment`

### 1.5 预览文件

- 方法：`GET`
- 路径：`/files/{fileId}/preview`
- 返回：文件流，`Content-Disposition: inline`

### 1.6 删除文件

- 方法：`DELETE`
- 路径：`/files/{fileId}`
- 说明：逻辑删除，`file_record.status` 置为 `DELETED`。

## 2. 统计报表服务

统计接口均要求 `TEACHER` 或 `ADMIN` 角色。

| 接口 | 方法 | 说明 |
|---|---|---|
| `/reports/overview` | GET | 获取总览卡片数据 |
| `/reports/material-status` | GET | 按材料状态统计 |
| `/reports/process-status` | GET | 按学生流程状态统计 |
| `/reports/certificate-status` | GET | 按证明类型和状态统计 |
| `/reports/upload-trend?days=7` | GET | 最近 N 天文件上传趋势 |
| `/reports/export?type=overview` | GET | 导出 CSV 报表 |

`type` 支持：`overview`、`material-status`、`process-status`、`certificate-status`、`upload-trend`。

## 3. 数据库脚本

脚本位置：

- `db/kingbase/004_member_d_file_report.sql`
- `member-c/db/004_member_d_file_report.sql`

核心表：`file_record`。为了便于 A/B 未完全合并时演示统计接口，脚本还包含 `material`、`student_process`、`certificate_application` 的 `CREATE TABLE IF NOT EXISTS` 兼容定义。
