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
│   ├── 成员A_流程审批模块_建表.sql      (成员A)
│   └── 成员B_电子证明模块_建表.sql      (成员B)
├── docs/                     # 接口文档
│   ├── 成员A_流程审批模块_接口说明书.txt
│   └── 成员B_电子证明模块_接口说明书.txt
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

### 待完成

- [ ] 学生进度总览关联流程审核/材料表（待成员A提供接口）
- [ ] 通知管理页面（后端 API 已就绪）
- [ ] 文件上传模块（占位接口已写，待成员D实现）
- [ ] 与成员A联调材料审核通知联动
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
