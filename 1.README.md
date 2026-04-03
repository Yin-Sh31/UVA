# NoPerson 农业无人机服务平台

## 项目概述

NoPerson 是一个综合性的农业无人机服务平台，连接农户、飞手（无人机操作员）和机主，提供农田喷洒、巡检等服务的在线预约和管理系统。

### 核心功能

- **农户端**：发布喷洒/巡检需求、查看服务进度、支付订单、评价服务
- **飞手端**：浏览需求大厅、接单作业、上传作业报告、设备管理
- **机主端**：设备出租管理、收益统计
- **管理后台**：用户管理、订单审核、设备审核、数据统计

## 技术架构

### 后端技术栈
- **框架**: Spring Boot 3.5.5
- **语言**: Java 17
- **数据库**: MySQL 8.0+
- **缓存**: Redis
- **ORM**: MyBatis-Plus 3.5.5
- **安全**: Spring Security + JWT (io.jsonwebtoken)
- **API 文档**: SpringDoc OpenAPI 2.2.0
- **工具库**: Hutool, Lombok, Jsoup
- **文件存储**: 阿里云 OSS

### 前端技术栈

#### 1. Web 前端 (农户/飞手/机主)
- **框架**: Vue 3.4.21 + Vite 4.x
- **UI 组件**: Element Plus 2.6.1
- **状态管理**: Pinia 2.1.7
- **路由**: Vue Router 4.3.0
- **HTTP 客户端**: Axios 1.6.7

#### 2. 管理后台前端
- **框架**: Vue 3.2.36 + Vite 4.x
- **UI 组件**: Element Plus 2.2.22
- **路由**: Vue Router 4.0.15
- **HTTP 客户端**: Axios 0.27.2

#### 3. 微信小程序
- **平台**: 微信小程序原生开发
- **渲染器**: Skyline + Glass-easel
- **功能模块**: 农户服务、飞手接单、支付、消息聊天

## 项目结构

```
noperson-finally/
├── src/main/java/com/cty/nopersonfinally/  # 后端源码
│   ├── controller/      # REST API 控制器 (29 个)
│   ├── service/         # 业务逻辑层
│   ├── mapper/          # MyBatis Mapper 接口 (30+)
│   ├── pojo/            # 实体类、DTO、VO
│   ├── config/          # 配置类 (Security, JWT, CORS 等)
│   └── utils/           # 工具类
├── frontend/            # Web 前端 (Vue 3)
│   ├── src/
│   │   ├── views/       # 页面组件
│   │   ├── components/  # 通用组件
│   │   ├── router/      # 路由配置
│   │   ├── store/       # 状态管理
│   │   └── utils/       # 工具函数
│   └── vite.config.js   # Vite 配置 (代理端口 3000)
├── admin-frontend/      # 管理后台前端 (Vue 3)
│   ├── src/
│   │   ├── views/       # 管理页面
│   │   ├── router/      # 路由配置
│   │   └── utils/       # 工具函数
│   └── vite.config.js   # Vite 配置 (代理端口 3001)
├── noperson/            # 微信小程序
│   ├── pages/           # 小程序页面
│   ├── components/      # 小程序组件
│   ├── utils/           # 工具函数
│   └── app.json         # 小程序配置
├── db_migration/        # 数据库迁移脚本
│   ├── noperson.sql     # 完整数据库结构
│   └── *.sql            # 增量更新脚本
└── upload/              # 本地文件上传目录
    ├── device/          # 设备图片
    ├── banner/          # 轮播图
    └── ...
```

## 主要业务模块

### 1. 用户认证模块
- 注册/登录 (JWT Token 认证)
- 角色选择 (农户/飞手/机主)
- 个人信息管理
- 头像上传

### 2. 需求订单模块
- 喷洒需求发布
- 巡检需求发布
- 订单状态流转 (待接取→处理中→作业中→待确认→已完成)
- 订单评价与投诉

### 3. 设备管理模块
- 无人机设备录入
- 设备租赁管理
- 设备维护记录
- 设备动态追踪

### 4. 支付模块
- 余额支付
- 交易记录
- 钱包管理
- 支付回调处理

### 5. 消息聊天模块
- 实时消息发送
- 会话列表
- 未读消息提醒
- 订单关联消息

### 6. AI 诊断模块
- 病虫害图像识别
- 诊断历史记录
- 作业建议生成

### 7. 地图与天气模块
- 地块边界标注
- 位置服务
- 天气预报查询

## API 接口概览

| 模块 | 路径前缀 | 说明 |
|------|---------|------|
| 认证 | /auth | 登录、注册、Token 刷新 |
| 用户 | /user | 用户信息管理 |
| 农户 | /farmer | 农户专属功能 |
| 飞手 | /flyer | 飞手专属功能 |
| 机主 | /owner | 机主专属功能 |
| 需求 | /demand | 需求发布与管理 |
| 设备 | /device | 设备 CRUD |
| 租赁 | /rental | 设备租赁 |
| 支付 | /payment | 支付相关 |
| 消息 | /message | 聊天消息 |
| 管理 | /admin | 后台管理接口 |

## 数据库设计

核心数据表 (30+ 张):
- `sys_user`: 用户基础表
- `farmer`, `flyer`, `user_owner`: 角色扩展表
- `demand`: 需求订单表
- `drone_device`: 无人机设备表
- `device_rental`: 设备租赁表
- `payment_record`, `transaction_record`: 支付交易表
- `message`, `conversation`: 消息会话表
- `banner`, `system_config`: 系统配置表

## 环境要求

- **JDK**: 17+
- **Node.js**: 16+ (前端)
- **MySQL**: 8.0+
- **Redis**: 6.0+
- **微信开发者工具**: 最新版 (小程序开发)

## 快速开始

详见 [部署指南.md](./部署指南.md)

## 开发说明

### 后端开发
```bash
cd noperson-finally
mvn spring-boot:run
# 服务运行在 http://localhost:8082
```

### Web 前端开发
```bash
cd frontend
npm install
npm run dev
# 访问 http://localhost:3000
```

### 管理后台开发
```bash
cd admin-frontend
npm install
npm run dev
# 访问 http://localhost:3001
```

### 小程序开发
使用微信开发者工具打开 `noperson` 目录

## 配置文件说明

后端需要配置以下环境变量或 application.yml:
- 数据库连接信息
- Redis 连接信息
- JWT 密钥
- 阿里云 OSS 配置
- 第三方 API 密钥 (高德地图、和风天气等)

## 常见问题

参考 [README_FLYER_PROFILE_DEBUG.md](./README_FLYER_PROFILE_DEBUG.md) 了解授权问题排查指南。

## 许可证

本项目为内部项目，请勿外传。
