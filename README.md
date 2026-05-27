# 学院学生综合服务与党团管理平台

面向学院学生事务与党团工作的综合服务平台，支持线上办理、材料审核、进度追踪、通知提醒和数据统计。学生端为微信小程序，管理端为 PC Web，采用前后端分离架构。

## 技术栈

| 层 | 技术 |
|---|------|
| 学生端 | 微信小程序（原生） |
| 管理端 | Vue 3 + TypeScript + Element Plus |
| 后端 | Spring Boot 3.2 + MyBatis-Plus + Spring Security + JWT |
| 数据库 | PostgreSQL / 人大金仓 Kingbase |
| 文件存储 | 本地磁盘 + Nginx |

## 团队分工

| 成员 | 职责 | 核心产出 |
|------|------|----------|
| **A** | 党团流程、材料提交与审核、通知引擎 | `process`/`material`/`approval_record` 表，流程 API，材料审核工作台 |
| **B** | 学生端前端 + 电子证明模块 | 小程序页面、`certificate_application` 表、证明办理全链路 |
| **C** | 用户系统、通知服务、操作日志 | 登录认证、权限校验、通知/待办 API、Kingbase 部署 |
| **D** | 文件存储、统计报表、代码管理 | 文件上传下载服务、统计报表、Git 仓库管理、前端工程化 |

## 项目结构

```
├── db/kingbase/                    # 数据库建表脚本（按编号顺序执行）
│   ├── 001_init_user_core.sql             (C) 用户核心表
│   ├── 002_seed_admin_user.sql            (C) 默认管理员
│   ├── 003_member_b_certificate.sql       (B) 证书申请表
│   ├── 004_member_d_file_report.sql       (D) 文件存储 + 兼容桩
│   ├── 005_seed_process_data.sql          (A) 党团流程种子数据
│   ├── 006_cleanup_duplicate_processes.sql
│   ├── 007_update_required_material_json.sql
│   ├── 008_add_user_avatar.sql
│   └── 成员A_流程审批模块_建表.sql         (A) 流程审批核心表
│
├── docs/                           # 文档
│   ├── 项目使用文档.md                     用户手册
│   ├── permission_matrix.md               权限矩阵
│   ├── 成员A_流程审批模块_接口说明书.txt
│   ├── 成员B_电子证明模块_接口说明书.txt
│   ├── 成员D_文件统计模块_接口说明书.md
│   ├── 成员D_Git仓库管理与合并审查说明.md
│   ├── 成员D_前端工程化说明.md
│   └── 成员D_联调验收清单.md
│
├── miniprogram/                    # 学生端微信小程序
│   ├── app.js / app.json / app.wxss     入口配置
│   ├── components/
│   │   ├── material-upload/              材料上传组件（校验 + 进度条）
│   │   └── timeline/                     时间轴组件
│   ├── pages/
│   │   ├── login/                        登录页
│   │   ├── index/                        首页
│   │   ├── progress/ + detail/           党团进度 + 详情
│   │   ├── upload/                       材料上传
│   │   ├── certificate/apply|list|detail 电子证明
│   │   ├── qa/                           智能问答
│   │   └── mine/                         个人中心
│   ├── utils/
│   │   ├── api.js                        API 封装（JWT、上传、下载）
│   │   └── validator.js                  文件校验
│   └── images/                           Tab 图标 + 默认头像
│
├── member-c/                       # 后端 + 管理端前端
│   ├── pom.xml                           Maven 配置
│   ├── src/main/java/com/ischool/isp/    Java 后端
│   │   ├── controller/                   15 个 REST 控制器
│   │   ├── service/ + impl/              业务逻辑层
│   │   ├── entity/                       实体（user/process/material/certificate/...）
│   │   ├── mapper/                       MyBatis-Plus 数据访问
│   │   ├── dto/request/ + response/      请求/响应 DTO
│   │   ├── config/                       配置（Security/MBP/CORS/...）
│   │   └── security/                     JWT 过滤器 + 工具类
│   ├── src/main/resources/
│   │   ├── application.yml               公共配置
│   │   ├── application-dev.yml           开发环境
│   │   └── application-prod.yml          生产环境
│   ├── admin-web/                        Vue 3 管理端
│   │   ├── src/api/                      7 个 API 模块
│   │   ├── src/views/                    11 个页面组件
│   │   ├── src/router/                   路由 + 角色守卫
│   │   └── src/stores/                   Pinia 认证状态
│   └── nginx-isp.conf                    Nginx 配置
│
├── scripts/                        # 辅助脚本（D）
└── README.md
```

## 接口基准

- **Base URL**: `/api`
- **认证**: `Authorization: Bearer <JWT>`（24h 有效）
- **统一响应**: `{ "code": 200, "message": "OK", "data": {...}, "timestamp": ... }`
- **分页响应**: `{ "records": [...], "total": N, "page": 1, "pageSize": 10, "pages": M }`

## 成员A — 业务流程与审批核心

### 接口

| 模块 | 前缀 | 端点 |
|------|------|------|
| 学生端 | `/api/student/` | 流程列表、发起流程、进度详情、节点材料要求、提交材料、时间轴 |
| 管理端 | `/api/admin/processes/` | 模板 CRUD、节点配置 |
| 管理端 | `/api/admin/materials/` | 材料筛选、详情、审核（通过/退回） |
| 内部事件 | `/api/internal/notification-events` | 材料提交/审核事件 → 创建通知 |

### 数据库

`process`、`process_node`、`student_process`、`material`、`approval_record`

### 状态

- [x] 数据库设计与建表 SQL
- [x] 后端流程/材料/审核接口实现
- [x] 管理端「流程模板管理」「材料审核」页面
- [x] 通知事件对接（MATERIAL_SUBMITTED / MATERIAL_AUDITED）
- [x] 流程种子数据（入党/入团 + 材料要求 JSON）
- [x] 材料审核修复（MaterialMapper SQL 语法修复）

## 成员B — 学生端体验与证明办理

### 小程序页面

| 页面 | 功能 |
|------|------|
| 登录 | 用户名密码登录 |
| 首页 | 问候语、党团进度入口、快捷服务 |
| 党团进度 + 详情 | 流程列表 → 节点状态 → 时间轴 → 材料提交 |
| 材料上传 | 材料清单、格式/大小校验、上传进度条、自动上传 |
| 电子证明（申请/列表/详情） | 类型选择、附件上传、状态筛选、进度节点、PDF 下载 |
| 个人中心 | 用户信息、头像上传、修改密码、退出登录 |
| 智能问答 | 常见问题快捷入口 + 对话式搜索，11 类本地知识库 |

### 数据库

`certificate_application`（5 种类型，4 状态流转）

### 后端

证书模块已实现，学生端 6 + 管理端 5 个接口，对接通知事件（CERT_APPLICATION_SUBMITTED / AUDITED / ISSUED）

### 状态

- [x] 小程序 9 页面 + 2 组件全部完成
- [x] 数据库建表 SQL
- [x] 后端证书模块（entity/mapper/service/controller）
- [x] 管理端「电子证明管理」页面
- [x] 文件上传对接 D 的服务
- [x] 智能问答知识库
- [x] 头像上传 + 修改密码
- [x] Tab 图标制作

## 成员C — 用户系统、通知与日志

### 接口

| 模块 | 前缀 | 端点 |
|------|------|------|
| 认证 | `/api/auth` | 登录(JWT)、获取用户信息、修改密码、更新头像、登出 |
| 用户管理 | `/api/users` | ADMIN CRUD、角色关联、密码重置 |
| 通知 | `/api/notifications` | 列表、未读数(NOTICE/TODO)、标记已读 |
| 日志 | `/api/logs` | ADMIN 分页查询、筛选 |
| 学生进度 | `/api/admin/students/progress` | TEACHER/ADMIN 筛选 |

### 数据库

`user`、`student`、`teacher`、`notification`、`operation_log`（+ `user.avatar_url`）

### 状态

- [x] 62+ Java 文件，Spring Boot 3.2 全栈实现
- [x] Spring Security + JWT + RBAC 权限体系
- [x] `@OpLog` AOP 审计日志（异步写入）
- [x] 管理端（登录/首页/用户管理/学生进度/系统日志/个人中心）
- [x] 路由守卫 + 角色菜单过滤
- [x] 默认 ADMIN 种子数据
- [x] Nginx 配置
- [x] 头像字段 + 自改密码接口
- [x] 通知事件接收扩展（CERT_APPLICATION_*）

## 成员D — 数据统计与文件服务

### 接口

| 模块 | 前缀 | 端点 |
|------|------|------|
| 文件 | `/api/files` | 上传、列表、预览、下载、删除 |
| 报表 | `/api/reports` | 概览、材料/流程/证明状态、上传趋势、CSV 导出 |

### 数据库

`file_record`

### 状态

- [x] 文件存储服务（本地 `uploads/` + 元数据入库）
- [x] 统计分析报表（5 个维度 + CSV 导出）
- [x] 管理端「文件管理」「统计分析报表」页面
- [x] 前端工程化（`.editorconfig`、`.gitattributes`、API 封装、工具函数）
- [x] Git 管理（`.gitmessage`、`CONTRIBUTING.md`、辅助脚本）
- [x] 小程序端文件上传对接

## 跨团队集成矩阵

| 对接方向 | 内容 | 状态 |
|---------|------|------|
| A → C | MATERIAL_SUBMITTED / MATERIAL_AUDITED 通知事件 | [x] 已对接 |
| B → C | CERT_APPLICATION_SUBMITTED / AUDITED / ISSUED 通知事件 | [x] 已对接 |
| B → D | 小程序材料/证书附件上传调用 `/api/files/upload` | [x] 已对接 |
| A → D | 材料 file_url 统一走文件服务 | [x] 已实现 |
| C → 全部 | JWT 认证 + RBAC（STUDENT/TEACHER/ADMIN） | [x] 已就绪 |
| C → 全部 | 通知服务（创建/查询/标记已读） | [x] 已就绪 |
| C → 全部 | 操作日志（`@OpLog` 自动记录） | [x] 已就绪 |
| Security | `/api/student/certificates/**` → STUDENT | [x] 已配置 |
| Security | `/api/reports/**` → TEACHER/ADMIN | [x] 已配置 |
| Security | `/api/files/**` → 需认证 | [x] 已配置 |

## 快速开始

详见 [docs/项目使用文档.md](docs/项目使用文档.md)

### 后端

```bash
cd member-c
mvn spring-boot:run                                # 开发模式

mvn clean package -DskipTests                       # 生产打包
java -jar target/isp-1.0.0-SNAPSHOT.jar --spring.profiles.active=prod
```

### 管理端

```bash
cd member-c/admin-web
npm install
npm run dev                                         # → http://localhost:3000
npm run build                                       # → dist/
```

### 小程序

1. [下载微信开发者工具](https://developers.weixin.qq.com/miniprogram/dev/devtools/download.html)
2. 导入 `miniprogram/` 目录，AppID 选测试号
3. 勾选「不校验合法域名」
4. 修改 `app.js` 中 `baseUrl` 为后端地址
5. 编译运行

### 数据库初始化

按编号顺序执行 `db/kingbase/` 下 001~008 共 8 个 SQL 文件。

### 默认账号

| 用户名 | 密码 | 角色 |
|--------|------|------|
| `admin` | `admin123` | 管理员 |
| `student1` | 通过管理端创建/重置 | 学生 |
