# mall — 电商微服务系统

基于 Spring Cloud Alibaba 微服务架构的电商后端系统，配套 Vue 3 + TypeScript 前端管理后台。

---

## 项目简介

本项目是一个功能完整的电商平台，涵盖商品管理、订单管理、用户认证、店铺管理、购物车、搜索等核心业务模块。采用微服务架构，各服务独立部署、独立维护，通过 Nacos 实现服务注册与配置管理。

---

## 技术栈

| 组件 | 版本 |
|------|------|
| Spring Boot | 3.0.2 |
| JDK | 17 |
| Spring Cloud | 2022.0.5 |
| Spring Cloud Alibaba | 2022.0.0.0 |
| MyBatis-Plus | 3.5.5 |
| MySQL | 8.x |
| Redis | 6.2.6 |
| RabbitMQ | 4.2.5 |
| Elasticsearch | 7.12.1 |
| Nacos | 2.2.3 |
| Druid | 1.2.20 |
| Vue 3 + TypeScript | - |
| Element Plus | 2.13+ |

---

## 系统架构

```
┌──────────────────────────────────────────────────────────┐
│                     客户端 (mall-vue)                      │
└────────────────────────────┬─────────────────────────────┘
                             │
                     ┌───────▼────────┐
                     │  mall-gateway  │  Spring Cloud Gateway
                     │  端口: 8080    │  路由 / 鉴权 / 限流 / 跨域
                     └───────┬────────┘
                             │
          ┌──────────────────┼──────────────────────────┐
          │                  │                          │
    ┌─────▼─────┐    ┌──────▼──────┐           ┌───────▼──────┐
    │ mall-auth │    │ mall-user   │           │ mall-store   │
    │ 8081      │    │ 8082        │           │ 8083         │
    │ 认证授权   │◄───│ 用户服务     │           │ 店铺服务      │
    └───────────┘    └──────┬──────┘           └──────┬───────┘
                            │                         │
                     ┌──────▼──────┐           ┌───────▼──────┐
                     │ mall-order  │           │ mall-product │
                     │ 8084        │           │ 8085         │
                     │ 订单服务      │           │ 商品服务      │
                     └─────────────┘           └──────────────┘
```

### 服务端口分配

| 服务 | 端口 | 职责 |
|------|------|------|
| mall-gateway | 8080 | API 网关，路由转发 + JWT 校验 + 限流 |
| mall-auth | 8081 | 认证授权，登录注册 + Token 签发 + 权限管理 |
| mall-user | 8082 | 用户管理，用户信息 CRUD + 地址管理 |
| mall-store | 8083 | 店铺管理，店铺 CRUD + 开店审核 + 横幅广告 |
| mall-order | 8084 | 订单管理，订单 + 购物车 + 物流 + 库存 |
| mall-product | 8085 | 商品管理，SPU/SKU + 品牌/分类 + 属性 + 搜索 |

---

## 项目结构

```
mall/
├── mall-backend/                     # 后端微服务
│   ├── mall-common/                  # 公共模块（工具类、常量、DTO）
│   ├── mall-gateway/                 # API 网关
│   ├── mall-auth/                    # 认证授权服务
│   ├── mall-user/                    # 用户服务
│   ├── mall-store/                   # 店铺服务
│   ├── mall-order/                   # 订单服务（含购物车、库存）
│   ├── mall-product/                 # 商品服务（含 ES 搜索）
│   └── pom.xml                       # 父 POM，统一版本管理
│
├── mall-frontend/                    # 前端
│   └── ecommerce-frontend/           # Vue 3 + TypeScript 管理后台
│
└── docs/                             # 项目文档
```

---

## 环境准备

### 1. 安装依赖

- **JDK 17+** — [下载](https://adoptium.net/)
- **Maven 3.8+** — [下载](https://maven.apache.org/download.cgi)
- **Node.js 20+** — [下载](https://nodejs.org/)
- **Docker & Docker Compose** — 用于部署中间件

### 2. 中间件部署

所有中间件通过 Docker 部署在同一网络 `cyh-net` 中，服务器 IP 以 `192.168.31.140` 为例。

#### 创建 Docker 网络

```bash
docker network create cyh-net
```

#### 部署 Nacos（注册中心 + 配置中心）

```bash
docker run -d \
  --name nacos-server \
  --restart=unless-stopped \
  --network cyh-net \
  -p 8848:8848 \
  -p 9848:9848 \
  -p 9849:9849 \
  -e MODE=standalone \
  -e PREFER_HOST_MODE=hostname \
  -e NACOS_AUTH_ENABLE=false \
  -v /home/nacos/logs:/home/nacos/logs \
  -v /home/nacos/data:/home/nacos/data \
  nacos/nacos-server:v2.2.0
```

访问控制台：http://192.168.31.140:8848/nacos（默认账号：nacos / nacos）

#### 部署 MySQL

```bash
docker run -d \
  --name mysql \
  --network cyh-net \
  -p 3306:3306 \
  -e MYSQL_ROOT_PASSWORD=123456 \
  -e MYSQL_DATABASE=ecommerce_platform \
  --restart unless-stopped \
  mysql:8.0
```

#### 部署 Redis（共享缓存）

```bash
docker run -d \
  --name redis \
  --network cyh-net \
  -p 6379:6379 \
  --restart unless-stopped \
  -v /home/redis/data:/data \
  redis:6.2.6 \
  redis-server --appendonly yes
```

#### 部署 Redis（订单缓存专用）

```bash
docker run -d \
  --name redis-order \
  --network cyh-net \
  -p 6380:6379 \
  --restart unless-stopped \
  -v /home/redis-order/data:/data \
  redis:6.2.6 \
  redis-server --appendonly yes
```

#### 部署 RabbitMQ

```bash
docker run -d \
  --name mq \
  --network cyh-net \
  --hostname rabbitmq-host \
  -p 5672:5672 \
  -p 15672:15672 \
  -e RABBITMQ_DEFAULT_USER=admin \
  -e RABBITMQ_DEFAULT_PASS=admin \
  -v rabbitmq_data:/var/lib/rabbitmq \
  --restart unless-stopped \
  rabbitmq:4.2.5-management
```

访问控制台：http://192.168.31.140:15672（默认账号：admin / admin）

#### 部署 Elasticsearch

```bash
docker run -d \
  --name es \
  -e "ES_JAVA_OPTS=-Xms512m -Xmx512m" \
  -e "discovery.type=single-node" \
  -v es-data:/usr/share/elasticsearch/data \
  -v es-plugins:/usr/share/elasticsearch/plugins \
  --privileged \
  --network cyh-net \
  -p 9200:9200 \
  -p 9300:9300 \
  --restart unless-stopped \
  elasticsearch:7.12.1
```

### 3. Nacos 配置中心

#### 创建共享配置

在 Nacos 控制台 → 配置管理 → 配置列表 → 选择命名空间 → 新建配置：

- **Data ID**：`mall-shared-config.yml`
- **Group**：`DEFAULT_GROUP`
- **配置格式**：YAML

配置内容如下（根据实际 IP 替换）：

```yaml
spring:
  datasource:
    type: com.alibaba.druid.pool.DruidDataSource
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://192.168.31.140:3306/ecommerce_platform?serverTimezone=Asia/Shanghai&useUnicode=true&characterEncoding=utf-8&zeroDateTimeBehavior=convertToNull&useSSL=false&allowPublicKeyRetrieval=true
    username: root
    password: 123456
    druid:
      initial-size: 5
      min-idle: 5
      max-active: 20
      max-wait: 60000
      time-between-eviction-runs-millis: 60000
      min-evictable-idle-time-millis: 300000
      validation-query: SELECT 1
      test-while-idle: true
      test-on-borrow: false
      test-on-return: false
      pool-prepared-statements: true
      max-pool-prepared-statements-per-connection-size: 20

  data:
    redis:
      host: 192.168.31.140
      port: 6379
      timeout: 5000ms
      lettuce:
        pool:
          max-active: 16
          max-idle: 8
          min-idle: 4
          max-wait: 3000ms
  cache:
    redis:
      time-to-live: 180000

  rabbitmq:
    host: 192.168.31.140
    port: 5672
    username: admin
    password: admin
    virtual-host: /
    listener:
      simple:
        acknowledge-mode: manual
        prefetch: 5
        retry:
          enabled: true
          max-attempts: 3
          initial-interval: 1000
          multiplier: 2
        default-requeue-rejected: false

  cloud:
    nacos:
      discovery:
        server-addr: 192.168.31.140:8848
        namespace: 6e152ee2-97b5-461d-aa45-32f1fb13a5ca
        group: DEFAULT_GROUP
    sentinel:
      transport:
        dashboard: localhost:8718
      eager: true
```

#### 创建订单服务专属配置

为 order 模块单独创建配置文件（覆盖 Redis 端口为 6380）：

- **Data ID**：`mall-order.yml`
- **Group**：`DEFAULT_GROUP`
- **配置格式**：YAML

```yaml
spring:
  data:
    redis:
      port: 6380
```

---

## 启动项目

### 后端服务

按以下顺序启动微服务：

```bash
# 1. 编译打包
cd mall-backend
mvn clean package -DskipTests

# 2. 启动网关（必须先启动）
java -jar mall-gateway/target/mall-gateway-1.0.0.jar

# 3. 启动认证服务（依赖网关）
java -jar mall-auth/target/mall-auth-1.0.0.jar

# 4. 启动其他服务（无先后顺序）
java -jar mall-user/target/mall-user-1.0.0.jar
java -jar mall-store/target/mall-store-1.0.0.jar
java -jar mall-product/target/mall-product-1.0.0.jar
java -jar mall-order/target/mall-order-1.0.0.jar
```

或者直接在 IDEA 中依次运行各服务的 `MallXxxApplication.java` 主类。

### 前端项目

```bash
cd mall-frontend/ecommerce-frontend
npm install
npm run dev
```

启动后访问 http://localhost:5173。

---