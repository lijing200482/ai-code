# AI Code Father

一个基于 Spring Boot 3 + Vue 3 的 **AI 驱动的网站应用生成平台**。用户可以通过与 AI 对话，自然语言描述需求，自动生成完整的网站应用（HTML 单页 / 多文件项目 / Vue 项目），并支持在线预览、部署和下载。

## 技术栈

### 后端

| 技术 | 说明 |
|------|------|
| Java 21 + Spring Boot 3.5 | 核心框架 |
| MyBatis-Flex 1.11 | ORM 数据访问层 |
| LangChain4j 1.16 | AI 能力集成（DeepSeek V4 Flash） |
| Knife4j 4.4 | API 文档（Swagger UI） |
| Redis + Spring Session | 会话存储、聊天记忆 |
| Redisson 3.50 | 分布式限流 |
| Tencent COS | 对象存储（应用部署产物） |
| Selenium 4.33 | 网页截图 |
| HikariCP | 数据库连接池 |
| Lombok | 代码简化 |

### 前端

| 技术 | 说明 |
|------|------|
| Vue 3.5 + TypeScript | 核心框架 |
| Vite 7 | 构建工具 |
| Ant Design Vue 4.2 | UI 组件库 |
| Pinia | 状态管理 |
| Vue Router 4.5 | 路由 |
| Axios | HTTP 客户端 |
| Markdown-it + highlight.js | 消息渲染与代码高亮 |

## 项目结构

`
ai-code/
├── ai-code-frontend/          # 前端项目（Vue 3 + Vite）
├── src/
│   ├── main/java/com/zijing/aicode/
│   │   ├── ai/                # AI 代码生成核心（模型配置、工具调用、安全护栏）
│   │   ├── annotation/        # 自定义注解（权限校验等）
│   │   ├── aop/               # AOP 切面（权限拦截等）
│   │   ├── config/            # 配置类（COS、Redis、ChatModel 路由等）
│   │   ├── constant/          # 常量定义
│   │   ├── controller/        # REST API 控制器
│   │   ├── core/              # 代码生成核心（解析器、保存器、门面）
│   │   ├── entity/            # 数据实体（PO/DTO/VO/枚举）
│   │   ├── exception/         # 全局异常处理
│   │   ├── generator/         # 代码生成器（MyBatis 逆向）
│   │   ├── langgraph4j/       # LangGraph 相关
│   │   ├── manager/           # 业务管理器（COS 管理等）
│   │   ├── mapper/            # MyBatis-Flex Mapper
│   │   ├── ratelimter/        # 限流组件（Redisson + AOP）
│   │   ├── service/           # 业务服务层
│   │   └── utils/             # 工具类
│   └── main/resources/
│       ├── mapper/            # MyBatis XML 映射
│       ├── prompt/            # AI 系统提示词模板
│       ├── sql/               # 建表 SQL
│       └── application*.yml   # 环境配置文件
└── pom.xml                    # Maven 依赖配置
`

## 功能特性

- **AI 对话生成应用**：通过自然语言描述需求，AI 自动生成 HTML / 多文件 / Vue 项目代码
- **流式输出**：SSE 实时推送 AI 生成内容与对话消息
- **代码解析与保存**：自动解析 AI 返回的代码，按类型分发到对应处理器
- **应用预览**：生成的静态资源本地可访问，右侧面板实时展示效果
- **应用部署**：将应用产物上传至腾讯云 COS，获取公开访问链接
- **应用截图**：基于 Selenium 自动生成应用预览截图
- **应用管理**：用户管理自己的应用（增删改查），管理员可管理全部应用
- **精选应用**：管理员可将优质应用标记为精选，用户在首页浏览
- **聊天记忆**：基于 Redis 持久化对话历史，支持多轮迭代修改
- **安全防护**：输入/输出安全护栏（Guardrail），防止恶意 Prompt 注入
- **API 限流**：基于 Redisson 的分布式限流，保护 AI 接口
- **权限控制**：用户角色区分（user/admin），接口级权限拦截

## 数据表

| 表名 | 说明 |
|------|------|
| \user\ | 用户表（账号、密码、角色、头像等） |
| \pp\ | 应用表（名称、封面、代码类型、部署标识等） |
| \chat_history\ | 对话历史表（消息内容、类型、关联应用） |

建表 SQL 见 \src/main/resources/sql/create_table.sql\。

## 快速开始

### 前置要求

- JDK 21+
- Maven 3.8+
- MySQL 8.0+
- Redis 7.0+
- Node.js 18+（前端开发）

### 后端启动

`powershell
# 1. 初始化数据库
mysql -u root -p < src\main\resources\sql\create_table.sql

# 2. 修改配置（src/main/resources/application-local.yml）
#    - 更新 MySQL/Redis/COS/AI API Key 等配置

# 3. 启动项目
mvn spring-boot:run
`

后端默认运行在 \http://localhost:8445\，API 文档可通过 \http://localhost:8445/api/doc.html\ 访问。

### 前端启动

`powershell
cd ai-code-frontend
npm install
npm run dev
`

前端默认运行在 \http://localhost:5173\。

## AI 配置

本项目使用 LangChain4j 集成 DeepSeek API（也可替换为其他 OpenAI 兼容接口）。

关键配置项（\pplication-local.yml\）：

`yaml
langchain4j:
  open-ai:
    chat-model:
      api-key: sk-xxx
      base-url: https://api.deepseek.com
      model-name: deepseek-v4-flash
      max-tokens: 384000
`

## 代码生成流程

`
用户输入 Prompt
    ↓
AI 路由服务 (AiCodeGenTypeRoutingService)
    ↓
选择生成策略 → HTML / 多文件 / Vue 项目
    ↓
流式输出代码 + 工具调用（文件读写）
    ↓
代码解析器 (CodeParser)
    ↓
文件保存器 (CodeFileSaver)
    ↓
本地静态资源 / COS 部署
`

## API 接口

| 模块 | 路径前缀 | 说明 |
|------|----------|------|
| 应用管理 | \/api/app/\ | 应用的增删改查、部署 |
| 对话历史 | \/api/chat-history/\ | 对话记录查询、SSE 流式对话 |
| 用户管理 | \/api/user/\ | 注册、登录、信息更新 |
| 静态资源 | \/api/static/\ | 生成代码的静态文件访问 |

完整接口文档请访问 Swagger UI：\http://localhost:8445/api/doc.html\

## 环境变量

前端配置文件 \i-code-frontend/config/env.ts\，从 \env.example.ts\ 复制并修改后端 API 地址。

## License

MIT
