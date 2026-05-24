# 学院学生综合服务与党团管理平台

面向学院学生事务与党团工作的综合服务平台，支持线上办理、材料审核、进度追踪、通知提醒和数据统计。采用前后端分离架构，学生端为微信小程序，管理端为 PC Web。

## 技术栈

| 层 | 技术 |
|---|------|
| 学生端 | 微信小程序 |
| 管理端 | Vue |
| 后端 | Java / Python REST API |
| 数据库 | 人大金仓 Kingbase |
| 文件存储 | 文件存储服务（成员D负责） |

## 团队分工

| 成员 | 职责 | 核心产出 |
|------|------|----------|
| **A** | 党团流程、材料提交与审核、通知引擎 | `process`/`material`/`approval_record` 表，流程API，材料审核工作台 |
| **B** | 学生端前端 + 电子证明模块 | 小程序页面、`certificate_application` 表、证明办理全链路 |
| **C** | 用户系统、通知服务、操作日志 | 登录认证、权限校验、通知/待办API、Kingbase部署 |
| **D** | 文件存储、统计报表、代码管理 | 文件上传下载服务、统计报表、Git仓库管理 |

## 项目结构

```
├── db/kingbase/              # 数据库建表SQL
│   ├── 001_init_user_core.sql          (成员C)
│   ├── 004_member_d_file_report.sql     (成员D)
│   ├── 成员A_流程审批模块_建表.sql      (成员A)
│   └── 成员B_电子证明模块_建表.sql      (成员B)
├── docs/                     # 接口文档
│   ├── 成员A_流程审批模块_接口说明书.txt
│   ├── 成员B_电子证明模块_接口说明书.txt
│   └── 成员D_文件统计模块_接口说明书.md
├── A/                         # 成员A：流程审批模块（建表SQL + 接口说明书 + 需求文档）
├── miniprogram/              # 学生端微信小程序（成员B）
│   ├── app.js / app.json / app.wxss
│   ├── components/
│   │   ├── material-upload/  # 材料上传组件（格式校验+进度条）
│   │   └── timeline/         # 时间轴组件
│   ├── pages/
│   │   ├── index/            # 学生首页
│   │   ├── progress/         # 我的党团进度 + 详情
│   │   ├── upload/           # 材料上传页
│   │   ├── certificate/      # 电子证明（申请/列表/详情）
│   │   └── qa/               # 智能问答
│   └── utils/
│       ├── api.js            # API 请求封装
│       └── validator.js      # 文件校验工具
├── member-c/                  # 成员C：后端 + 管理端前端
│   ├── pom.xml               # Spring Boot 3.2 Maven 配置
│   ├── src/main/java/        # Java 源码（实体/服务/控制器/安全）
│   ├── src/main/resources/   # 配置文件
│   ├── admin-web/            # Vue 3 管理端（Element Plus）
│   │   ├── src/api/          # API 封装
│   │   ├── src/views/        # 登录/首页/用户管理/学生进度/系统日志
│   │   ├── src/router/       # 路由 + 角色守卫
│   │   └── src/stores/       # Pinia 状态管理
│   ├── db/                   # 数据库 SQL（DDL + 种子数据）
│   └── nginx-isp.conf        # Nginx 反向代理配置
└── README.md
```

## 成员A 当前进度

### 已完成

- **数据库设计** — `process`（流程模板）、`process_node`（流程节点）、`student_process`（学生流程记录）、`material`（材料）、`approval_record`（审核记录）五张核心业务表，含外键关联、状态约束和索引
- **接口说明书** — 覆盖 3 大模块 13 个 REST API：

| 模块 | 路径前缀 | 接口数 | 说明 |
|------|---------|--------|------|
| 学生端 | `/api/v1/student/` | 6 | 可办理流程、发起流程、进度详情、节点材料要求、提交材料、时间轴 |
| 管理端-模板 | `/api/v1/admin/processes/` | 4 | 流程模板 CRUD + 节点配置 |
| 管理端-审核 | `/api/v1/admin/materials/` | 3 | 材料筛选、详情、审核（通过/退回） |

- **通知触发设计** — 定义了 4 类事件 `MATERIAL_SUBMITTED` / `MATERIAL_AUDITED` / `STUDENT_PROCESS_NODE_CHANGED` / `STUDENT_PROCESS_COMPLETED`，统一通过 `POST /api/internal/notification-events` 调用成员C通知服务
- **工作流状态机** — 材料 `DRAFT → PENDING → APPROVED/RETURNED`，学生流程 `IN_PROGRESS → COMPLETED`

### 待完成

- [ ] 后端接口实现（建表 SQL 和接口说明书已完成）
- [ ] 管理端流程模板配置页
- [ ] 管理端材料审核工作台
- [ ] 与成员C联调通知事件接口
- [ ] 与成员B联调学生端页面接口
- [ ] 与成员D对接文件上传（material.file_url）

## 成员B 当前进度

### 已完成

- **数据库设计** — `certificate_application` 建表SQL，支持在学证明、请假申请、盖章申请、党员证明、成绩单证明 5 种类型，4 状态流转（PENDING → APPROVED → ISSUED / RETURNED）
- **接口说明书** — 12 个 REST API（学生端 6 个 + 管理端 5 个 + 统计 1 个），含通知事件契约和错误码
- **小程序前端** — 8 个页面 + 2 个可复用组件

| 页面 | 状态 | 说明 |
|------|------|------|
| 学生首页 | 完成 | 待办提醒、我的流程卡片、快速入口 |
| 我的党团进度 | 完成 | 流程列表 + 详情页（时间轴） |
| 材料上传 | 完成 | 材料清单、格式校验、上传进度条 |
| 电子证明申请 | 完成 | 类型选择、附件上传、表单提交 |
| 我的证明列表 | 完成 | 状态/类型筛选、列表展示 |
| 申请详情 | 完成 | 进度节点、退回原因、PDF下载 |
| 智能问答 | 完成 | 快捷问题 + 对话式交互 |

### 待完成

- [ ] Tab 栏图标制作（当前引用占位路径 `images/tab-*.png`）
- [ ] 与成员A联调材料上传接口
- [ ] 与成员D联调文件上传接口
- [ ] 对接成员C的登录认证
- [ ] 后端电子证明接口实现（可由成员B或其他成员负责）


## 成员D 当前进度

### 已完成

- **文件存储服务** — 已实现本地文件上传、下载、预览、逻辑删除、权限校验和文件元数据管理；核心接口位于 `member-c/src/main/java/com/ischool/isp/controller/FileController.java`。
- **数据库设计** — 新增 `file_record` 文件元数据表；脚本位置为 `db/kingbase/004_member_d_file_report.sql` 与 `member-c/db/004_member_d_file_report.sql`。
- **统计报表后端** — 已实现 `/api/reports/overview`、`/api/reports/material-status`、`/api/reports/process-status`、`/api/reports/certificate-status`、`/api/reports/upload-trend`、`/api/reports/export`。
- **管理端统计页面** — 新增 Vue 页面 `统计分析报表`，支持总览卡片、表格统计、简易条形图和 CSV 导出。
- **接口文档** — 新增 `docs/成员D_文件统计模块_接口说明书.md`。

### 对接说明

- A 组材料模块可在提交材料时调用 `POST /api/files/upload`，传入 `relatedType=MATERIAL` 和后续生成的 `relatedId`，返回的 `fileUrl` 可写入 `material.file_url`。
- B 组电子证明模块可复用同一上传接口，传入 `relatedType=CERTIFICATE`。
- C 组认证已经采用 `Authorization: Bearer <JWT>`，D 的文件与报表接口已按当前安全机制接入。

### 待联调

- [ ] 与 A 的真实 `material` / `student_process` 表联调统计查询。
- [ ] 与 B 的小程序上传组件联调 `fileId/fileUrl/previewUrl` 返回格式。
- [ ] 如课程要求正式文件服务器，可把当前本地 `uploads/` 存储替换为对象存储实现，接口不变。

## 成员C 当前进度

### 已完成

- **数据库设计** — `user`、`student`、`teacher`、`notification`、`operation_log` 五张核心表，含约束、索引、外键
- **后端服务（Spring Boot 3.2）** — 62 个 Java 文件，覆盖以下模块：

| 模块 | 路径 | 说明 |
|------|------|------|
| 认证 | `/api/auth` | 登录(JWT)、获取当前用户、登出 |
| 用户管理 | `/api/users` | ADMIN CRUD、角色关联（student/teacher）、密码重置 |
| 通知 | `/api/notifications` | 分页列表、未读数(NOTICE/TODO)、标记已读(归属校验)、创建 |
| 操作日志 | `/api/logs` | ADMIN 分页查询、按类型/结果/时间范围/关键字筛选 |
| 学生进度 | `/api/admin/students/progress` | TEACHER+ADMIN 按年级/班级/政治面貌筛选 |

- **安全机制** — Spring Security + JWT 无状态认证、URL 级 + `@PreAuthorize` 方法级 RBAC、STUDENT 数据隔离
- **审计日志** — `@OpLog` 注解 + AOP 切面自动记录关键操作，异步写入
- **管理端前端（Vue 3 + Element Plus）** — 16 个文件，含登录页、首页、用户管理、学生进度总览、系统日志查看
- **路由守卫** — 基于角色自动隐藏菜单项，未授权访问重定向首页
- **种子数据** — 默认 ADMIN 账号 `admin / admin123`，启动时自动修正密码哈希
- **Nginx 配置** — 反向代理 `/api/` 到后端、`/admin/` 到前端静态文件

### 本地运行

```bash
# 后端（需要 JDK 17 + Maven）
cd member-c
mvn spring-boot:run

# 管理端前端
cd member-c/admin-web
npm install
npm run dev          # → http://localhost:3000
```

### 跨团队集成

| 方向 | 提供/依赖 | 对接人 | 状态 |
|------|----------|--------|------|
| C 提供 | JWT 登录认证（`/api/auth/login`） | A、B | 已就绪 |
| C 提供 | 角色权限校验（STUDENT/TEACHER/ADMIN） | A、B | 已就绪 |
| C 提供 | 通知创建 API（`POST /api/notifications`） | A（审核后触发通知） | 已就绪 |
| C 提供 | 操作日志记录（`@OpLog` 注解） | A、B、D | 已就绪 |
| C 提供 | 学生进度数据（`GET /api/admin/students/progress`） | A（流程审核时可查学生） | 已就绪 |
| C 待提供 | 通知事件接收（`POST /api/internal/notification-events`） | A（`MATERIAL_SUBMITTED` 等事件） | 待新增 |
| C 依赖 | 学生流程/材料数据 | A（进度总览需关联 A 的业务表） | 待联调 |
| C 依赖 | 文件上传服务 | D（`/files/` 占位接口已写） | 待联调 |

### 待完成

- [ ] 新增 `POST /api/internal/notification-events` 接口，接收成员A的材料提交/审核事件，自动创建通知和待办
- [ ] 角色映射对齐（A 使用 COUNSELOR/LEAGUE_SECRETARY 等角色需映射到 C 的 TEACHER）
- [ ] 学生进度总览关联 `student_process` + `material` 表，展示材料审核进度
- [ ] 通知管理页面（后端 API 已就绪）
- [ ] 文件上传模块（占位接口已写，待成员D实现）
- [ ] Kingbase 生产环境部署

## 小程序本地运行

1. 下载 [微信开发者工具](https://developers.weixin.qq.com/miniprogram/dev/devtools/download.html)
2. 打开开发者工具，导入项目，选择 `miniprogram/` 目录
3. 填入 AppID（测试号或正式 AppID）
4. 修改 `app.js` 中的 `baseUrl` 为实际后端地址
5. 在开发者工具中预览/真机调试

## 接口基准

- Base URL: `/api`
- 认证: `Authorization: Bearer <JWT>`（登录接口返回，24h 有效）
- 统一响应格式: `{ "code": 200, "message": "OK", "data": {...}, "timestamp": 1779628947780 }`
- 分页响应: `{ "code": 200, "data": { "records": [...], "total": N, "page": 1, "pageSize": 10, "pages": M } }`
