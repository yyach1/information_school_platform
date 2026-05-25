# 成员D：前端工程化说明

本文档说明管理端 Vue 项目的统一目录、API 封装、环境变量和页面规范，避免后续成员扩展页面时风格不一致。

## 1. 管理端目录结构

```text
member-c/admin-web/
├── src/
│   ├── api/          # 后端接口封装
│   ├── components/   # 可复用组件
│   ├── router/       # 路由与权限守卫
│   ├── stores/       # Pinia 状态
│   ├── types/        # 通用类型定义
│   ├── utils/        # 格式化和工具函数
│   └── views/        # 页面
```

## 2. API 封装规则

- 所有后端请求统一写在 `src/api/`。
- 不在页面中直接写裸 `axios`。
- 文件下载、CSV 导出等二进制接口使用 `responseType: 'blob'`。
- API 返回值遵循后端统一格式，由 `request.ts` 拦截器取出 `data`。

## 3. 页面规范

- 页面根节点使用 `page-container` 类名。
- 查询条件放在顶部卡片。
- 表格分页统一使用 Element Plus `el-pagination`。
- 操作按钮顺序固定为：查看 / 下载 / 删除。
- 教师和管理员可访问统计与文件管理，学生端通过小程序访问。

## 4. 环境变量

本地开发可复制：

```bash
cp .env.example .env.local
```

默认配置：

```text
VITE_API_BASE_URL=/api
```

Vite 代理将 `/api` 转发到后端 `http://localhost:8080`。

## 5. 成员D新增页面

| 页面 | 路由 | 功能 |
|---|---|---|
| 文件管理 | `/files` | 查询、上传、预览、下载、删除文件 |
| 统计分析报表 | `/reports` | 总览、材料状态、流程状态、证明状态、上传趋势、CSV 导出 |

## 6. 自测步骤

```powershell
cd member-c/admin-web
npm install
npm run dev
```

浏览器打开：

- `http://localhost:3000/files`
- `http://localhost:3000/reports`

如需构建：

```powershell
npm run build
```
