# 二手猫闲置交易平台 - Code Wiki

## 1. 项目概述

二手猫是一个基于Spring Boot + Vue 3的二手商品交易平台，提供商品发布、浏览、搜索、购买、聊天、支付等完整功能，并集成AI智能客服辅助用户操作。

### 1.1 项目结构

```
期末项目/
├── db/                      # 数据库脚本
│   └── mall.sql             # 数据库初始化脚本
├── mall-client/             # 前端项目
│   ├── src/                 # 前端源代码
│   │   ├── apis/            # API接口配置
│   │   ├── components/      # 公共组件
│   │   ├── router/          # 路由配置
│   │   ├── stores/          # Pinia状态管理
│   │   ├── utils/           # 工具函数
│   │   └── views/           # 页面视图
│   ├── package.json         # 前端依赖配置
│   └── vite.config.js       # Vite配置
├── server/                  # 后端项目
│   ├── src/main/java/com/easy/ai/
│   │   ├── agent/           # AI代理（UsedCatAgent）
│   │   ├── common/          # 通用类（Result、PageBean）
│   │   ├── config/          # 配置类
│   │   ├── controller/      # REST控制器
│   │   ├── dto/             # 数据传输对象
│   │   ├── entity/          # 实体类
│   │   ├── exception/       # 全局异常处理
│   │   ├── interceptors/    # 拦截器
│   │   ├── mapper/          # MyBatis映射器
│   │   ├── service/         # 服务接口及实现
│   │   ├── tools/           # AI工具类
│   │   ├── utils/           # 工具类
│   │   ├── websocket/       # WebSocket处理
│   │   └── ServerApplication.java
│   ├── src/main/resources/
│   │   ├── db/              # 数据库脚本
│   │   ├── mapper/          # MyBatis XML映射
│   │   ├── prompts/         # AI提示词
│   │   └── application.properties
│   └── pom.xml              # Maven依赖配置
```

---

## 2. 技术栈

### 2.1 后端技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| Java | 17 | 开发语言 |
| Spring Boot | 3.2.6 | 应用框架 |
| MyBatis-Plus | 3.5.12 | ORM框架 |
| MySQL | 8.0+ | 数据库 |
| Redis | - | 缓存、验证码存储 |
| JWT | jjwt 0.12.6 + java-jwt 4.4.0 | 身份认证 |
| Spring AI | 1.1.7 | AI集成框架 |
| DeepSeek | - | 大语言模型 |
| 阿里百炼 | - | 大语言模型、向量嵌入 |
| Qdrant | - | 向量数据库 |
| Spring WebSocket | - | 实时聊天 |
| 支付宝沙箱SDK | 4.40.763 | 支付功能 |
| 快递100 SDK | 1.1.3 | 物流查询 |
| Spring Mail | - | 邮箱验证码 |
| SpringDoc OpenAPI | 2.5.0 | API文档 |

### 2.2 前端技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| Vue | 3.5.31 | 前端框架 |
| Vue Router | 5.0.4 | 路由管理 |
| Pinia | 3.0.4 | 状态管理 |
| Element Plus | 2.13.6 | UI组件库 |
| Axios | 1.14.0 | HTTP请求 |
| Vite | 8.0.3 | 构建工具 |
| WebSocket | - | 实时聊天 |

---

## 3. 核心模块职责

### 3.1 用户模块

**职责**：用户注册、登录、信息管理、密码重置、QQ第三方登录

**关键文件**：
- [UserController.java](file:///d:/hope_school/javaweb/期末项目/server/src/main/java/com/easy/ai/controller/UserController.java) - 用户相关REST接口
- [UserService.java](file:///d:/hope_school/javaweb/期末项目/server/src/main/java/com/easy/ai/service/UserService.java) - 用户服务接口
- [QqAuthController.java](file:///d:/hope_school/javaweb/期末项目/server/src/main/java/com/easy/ai/controller/QqAuthController.java) - QQ登录接口
- [JwtUtil.java](file:///d:/hope_school/javaweb/期末项目/server/src/main/java/com/easy/ai/utils/JwtUtil.java) - JWT工具类

**核心流程**：
1. 注册：邮箱验证 → 密码加密 → 保存用户
2. 登录：邮箱+密码验证 → 生成JWT Token
3. 密码重置：邮箱验证码 → 验证 → 更新密码

### 3.2 商品模块

**职责**：商品发布、查询、搜索、状态管理、图片上传

**关键文件**：
- [CommodityController.java](file:///d:/hope_school/javaweb/期末项目/server/src/main/java/com/easy/ai/controller/CommodityController.java) - 商品接口
- [CommodityTool.java](file:///d:/hope_school/javaweb/期末项目/server/src/main/java/com/easy/ai/tools/CommodityTool.java) - 商品业务工具
- [CategoryController.java](file:///d:/hope_school/javaweb/期末项目/server/src/main/java/com/easy/ai/controller/CategoryController.java) - 分类管理
- [FileUploadUtil.java](file:///d:/hope_school/javaweb/期末项目/server/src/main/java/com/easy/ai/utils/FileUploadUtil.java) - 文件上传工具

**核心流程**：
1. 发布商品：上传图片 → 保存商品信息 → 保存相册关联
2. 查询商品：分页查询 → 关联相册图片
3. 搜索：关键字模糊匹配商品名称/描述

### 3.3 订单模块

**职责**：订单创建、支付、发货、物流查询、取消订单

**关键文件**：
- [OrderController.java](file:///d:/hope_school/javaweb/期末项目/server/src/main/java/com/easy/ai/controller/OrderController.java) - 订单接口
- [OrderTool.java](file:///d:/hope_school/javaweb/期末项目/server/src/main/java/com/easy/ai/tools/OrderTool.java) - 订单业务工具
- [AlipayController.java](file:///d:/hope_school/javaweb/期末项目/server/src/main/java/com/easy/ai/controller/AlipayController.java) - 支付宝支付
- [ExpressService.java](file:///d:/hope_school/javaweb/期末项目/server/src/main/java/com/easy/ai/utils/ExpressService.java) - 物流查询服务

**订单状态**：
- 0：待支付
- 1：已支付
- 2：已发货
- 3：已取消

### 3.4 聊天模块

**职责**：用户间实时聊天、消息管理

**关键文件**：
- [ChatWebSocketHandler.java](file:///d:/hope_school/javaweb/期末项目/server/src/main/java/com/easy/ai/websocket/ChatWebSocketHandler.java) - WebSocket处理器
- [WebSocketConfig.java](file:///d:/hope_school/javaweb/期末项目/server/src/main/java/com/easy/ai/config/WebSocketConfig.java) - WebSocket配置
- [UserChatController.java](file:///d:/hope_school/javaweb/期末项目/server/src/main/java/com/easy/ai/controller/UserChatController.java) - 聊天消息接口

**核心流程**：
1. 建立WebSocket连接（携带JWT Token）
2. 发送消息 → 保存到数据库 → 推送到目标用户
3. 查询会话列表和历史消息

### 3.5 AI智能客服模块

**职责**：智能客服对话、商品描述美化、工具调用

**关键文件**：
- [UsedCatAgent.java](file:///d:/hope_school/javaweb/期末项目/server/src/main/java/com/easy/ai/agent/UsedCatAgent.java) - AI代理
- [ChatController.java](file:///d:/hope_school/javaweb/期末项目/server/src/main/java/com/easy/ai/controller/ChatController.java) - AI对话接口
- [ChatMemoryTool.java](file:///d:/hope_school/javaweb/期末项目/server/src/main/java/com/easy/ai/tools/ChatMemoryTool.java) - 对话记忆工具
- [AiPromptConfig.java](file:///d:/hope_school/javaweb/期末项目/server/src/main/java/com/easy/ai/config/AiPromptConfig.java) - 提示词配置

**AI工具**：
- [CommodityTool.java](file:///d:/hope_school/javaweb/期末项目/server/src/main/java/com/easy/ai/tools/CommodityTool.java) - 商品查询工具
- [OrderQueryTools.java](file:///d:/hope_school/javaweb/期末项目/server/src/main/java/com/easy/ai/tools/OrderQueryTools.java) - 订单查询工具
- [BrowseHistoryTool.java](file:///d:/hope_school/javaweb/期末项目/server/src/main/java/com/easy/ai/tools/BrowseHistoryTool.java) - 浏览历史工具
- [FavoriteTool.java](file:///d:/hope_school/javaweb/期末项目/server/src/main/java/com/easy/ai/tools/FavoriteTool.java) - 收藏工具

### 3.6 收藏与浏览模块

**职责**：商品收藏、浏览历史记录

**关键文件**：
- [FavoriteController.java](file:///d:/hope_school/javaweb/期末项目/server/src/main/java/com/easy/ai/controller/FavoriteController.java) - 收藏接口
- [BrowseHistoryController.java](file:///d:/hope_school/javaweb/期末项目/server/src/main/java/com/easy/ai/controller/BrowseHistoryController.java) - 浏览历史接口

---

## 4. 关键实体类说明

### 4.1 User（用户实体）

| 字段 | 类型 | 说明 |
|------|------|------|
| userId | Integer | 用户ID（主键） |
| email | String | 邮箱（唯一索引） |
| password | String | 密码（BCrypt加密） |
| nickname | String | 昵称 |
| avatar | String | 头像路径 |
| credit | Integer | 信誉分（默认10） |
| role | Integer | 角色（1管理员，2用户） |
| openId | String | QQ OpenID |
| provider | String | 登录来源（local/qq） |
| createTime | LocalDateTime | 创建时间 |

### 4.2 Commodity（商品实体）

| 字段 | 类型 | 说明 |
|------|------|------|
| commodityId | Integer | 商品ID（主键） |
| userId | Integer | 发布者ID |
| status | Integer | 状态（0下架，1上架，2售出） |
| commodityName | String | 商品名称 |
| commodityDesc | String | 商品描述 |
| brand | String | 品牌 |
| quality | Integer | 成色（1-10分） |
| useStatus | String | 使用状态描述 |
| price | double | 价格 |
| commodityType | Integer | 商品类型（关联category表） |
| browse | Integer | 浏览量 |
| createTime | LocalDateTime | 创建时间 |
| albums | List\<Album\> | 商品图片列表（非数据库字段） |

### 4.3 Order（订单实体）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Integer | 订单ID（主键） |
| orderNo | String | 订单号 |
| userId | Integer | 用户ID |
| commodityId | Integer | 商品ID |
| commodityName | String | 商品名称 |
| commodityDesc | String | 商品描述 |
| commodityImage | String | 商品图片 |
| price | double | 单价 |
| quantity | Integer | 数量 |
| totalAmount | double | 总金额 |
| addressId | Integer | 收货地址ID |
| consignee | String | 收货人 |
| phone | String | 联系电话 |
| address | String | 收货地址 |
| status | Integer | 订单状态 |
| payMethod | String | 支付方式 |
| tradeNo | String | 支付宝交易号 |
| payTime | LocalDateTime | 支付时间 |
| expressNo | String | 快递单号 |
| expressCompany | String | 快递公司编码 |

### 4.4 Conversation（会话实体）

| 字段 | 类型 | 说明 |
|------|------|------|
| conversationId | Integer | 会话ID |
| userId | Integer | 当前用户ID |
| targetUserId | Integer | 对方用户ID |
| commodityId | Integer | 关联商品ID |
| lastMessage | String | 最后一条消息 |
| lastTime | LocalDateTime | 最后消息时间 |
| unreadCount | Integer | 未读消息数 |

### 4.5 Message（消息实体）

| 字段 | 类型 | 说明 |
|------|------|------|
| messageId | Integer | 消息ID |
| conversationId | Integer | 会话ID |
| senderId | Integer | 发送者ID |
| receiverId | Integer | 接收者ID |
| content | String | 消息内容 |
| messageType | String | 消息类型（text/image） |
| isRead | Integer | 是否已读（0未读，1已读） |
| createTime | LocalDateTime | 发送时间 |

### 4.6 其他实体

| 实体 | 说明 |
|------|------|
| Category | 商品分类 |
| Album | 商品图片相册 |
| Favorite | 收藏记录 |
| BrowseHistory | 浏览历史 |
| ReceivingAddress | 收货地址 |
| Session | AI对话会话 |
| History | AI对话历史 |
| Role | 用户角色 |

---

## 5. 数据库表结构

### 5.1 表关系图

```
user (用户)
├── commodity (商品) → album (相册)
├── favorite (收藏) ← commodity
├── browse_history (浏览历史) ← commodity
├── receiving_address (收货地址)
├── orders (订单) ← commodity, receiving_address
├── conversation (会话) ← user(target_user_id)
├── message (消息) ← conversation
└── session (AI会话) → history (AI对话历史)

category (分类) → commodity
role (角色) → user
```

### 5.2 核心表DDL

#### user表

```sql
CREATE TABLE `user` (
  `user_id` int NOT NULL AUTO_INCREMENT COMMENT '用户id',
  `email` varchar(255) NOT NULL COMMENT '邮箱',
  `password` varchar(100) NOT NULL COMMENT '密码',
  `nickname` varchar(10) NULL COMMENT '昵称',
  `avatar` varchar(255) NULL COMMENT '头像',
  `credit` int NOT NULL DEFAULT 10 COMMENT '信誉分',
  `role` int NOT NULL DEFAULT 1 COMMENT '角色',
  `create_time` datetime NULL COMMENT '创建时间',
  `open_id` varchar(64) NULL COMMENT 'QQ openId',
  `provider` varchar(16) NULL DEFAULT 'local' COMMENT '登录来源',
  PRIMARY KEY (`user_id`),
  UNIQUE INDEX `email_index`(`email`)
) ENGINE = InnoDB;
```

#### commodity表

```sql
CREATE TABLE `commodity` (
  `commodity_id` int NOT NULL AUTO_INCREMENT COMMENT '商品id',
  `user_id` int NOT NULL COMMENT '所属用户id',
  `status` int NULL DEFAULT 1 COMMENT '商品状态',
  `commodity_name` varchar(100) NOT NULL COMMENT '商品名称',
  `commodity_desc` text NULL COMMENT '商品介绍',
  `brand` varchar(255) NULL COMMENT '品牌',
  `quality` int NULL COMMENT '成色1-10分',
  `use_status` varchar(255) NOT NULL COMMENT '使用状态',
  `price` decimal(10, 2) NOT NULL COMMENT '商品价格',
  `commodity_type` int NOT NULL COMMENT '商品类型',
  `browse` int NULL DEFAULT 0 COMMENT '浏览量',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`commodity_id`)
) ENGINE = InnoDB;
```

#### orders表

```sql
CREATE TABLE `orders` (
  `id` int NOT NULL AUTO_INCREMENT,
  `order_no` varchar(50) NULL COMMENT '订单号',
  `user_id` int NULL COMMENT '用户id',
  `commodity_id` int NULL COMMENT '商品id',
  `commodity_name` varchar(255) NULL COMMENT '商品名称',
  `commodity_desc` text NULL COMMENT '商品描述',
  `commodity_image` varchar(500) NULL COMMENT '商品图片',
  `price` double NULL COMMENT '单价',
  `quantity` int NULL DEFAULT 1 COMMENT '数量',
  `total_amount` double NULL COMMENT '总金额',
  `address_id` int NULL COMMENT '收货地址id',
  `consignee` varchar(50) NULL COMMENT '收货人',
  `phone` varchar(20) NULL COMMENT '联系电话',
  `address` varchar(255) NULL COMMENT '收货地址',
  `status` int NULL DEFAULT 0 COMMENT '订单状态',
  `pay_method` varchar(20) NULL COMMENT '支付方式',
  `trade_no` varchar(100) NULL COMMENT '支付宝交易号',
  `pay_time` datetime NULL COMMENT '支付时间',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `express_no` varchar(50) NULL DEFAULT '' COMMENT '快递单号',
  `express_company` varchar(50) NULL DEFAULT '' COMMENT '快递公司编码',
  PRIMARY KEY (`id`)
) ENGINE = InnoDB;
```

---

## 6. API接口清单

### 6.1 用户接口

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 发送验证码 | GET | `/user/sendCode?email=xxx` | 注册验证码 |
| 登录 | POST | `/user/login` | 用户登录 |
| 注册 | POST | `/user/register` | 用户注册 |
| 获取当前用户 | GET | `/user/getCurrUser` | 通过Token获取 |
| 根据ID获取用户 | GET | `/user/getUserByUserId?userId=xxx` | 获取商品发布者信息 |
| 更新用户信息 | POST | `/user/updateUser` | 更新昵称等 |
| 更新头像 | POST | `/user/updateAvatar` | 上传头像 |
| 重置密码 | POST | `/user/resetPassword` | 验证旧密码改新密码 |
| 忘记密码验证码 | GET | `/user/sendForgetCode` | 发送忘记密码验证码 |
| 忘记密码 | POST | `/user/forgotPassword` | 通过验证码重置密码 |

### 6.2 商品接口

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 获取商品详情 | GET | `/commodity/getCommodityById?commodityId=xxx` | 获取商品详情及图片 |
| 获取商品列表 | GET | `/commodity/getCommodityList` | 分页查询 |
| 新增商品 | POST | `/commodity/addCommodity` | 发布闲置商品 |
| 上传商品图片 | POST | `/commodity/uploadCommunityImg` | 上传图片 |
| 更新商品状态 | POST | `/commodity/updateStatus` | 上架/下架/售出 |
| 删除商品 | POST | `/commodity/delete` | 删除商品 |
| 搜索商品 | GET | `/commodity/search?keyword=xxx` | 关键字搜索 |
| 增加浏览量 | POST | `/commodity/incrementBrowse` | 浏览量+1 |

### 6.3 订单接口

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 创建订单 | POST | `/order/create` | 创建新订单 |
| 获取订单列表 | GET | `/order/list?userId=xxx` | 获取用户订单 |
| 获取订单详情 | GET | `/order/{id}` | 获取单个订单 |
| 更新订单状态 | PUT | `/order/updateStatus` | 更新订单状态 |
| 查询物流 | GET | `/order/logistics?orderId=xxx` | 查询物流信息 |
| 获取卖家订单 | GET | `/order/sellerList?userId=xxx` | 获取卖家待发货订单 |
| 发货 | PUT | `/order/ship` | 填写物流单号 |
| 取消订单 | PUT | `/order/cancel` | 取消待支付订单 |

### 6.4 AI客服接口

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 获取会话历史 | GET | `/ai/getCurrSessionHistoryList?sessionId=xxx` | 获取AI对话历史 |
| 初始化客服会话 | GET | `/ai/customerServiceInit?userId=xxx` | 获取或创建会话 |
| 客服流式对话 | GET | `/ai/customerServiceChat` | AI流式回复 |
| 美化商品描述 | POST | `/ai/beautifyDescription` | AI优化文案 |

### 6.5 支付接口

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 支付宝下单 | POST | `/alipay/create` | 创建支付宝订单 |
| 支付宝回调 | POST | `/alipay/notify` | 支付结果异步通知 |
| 支付结果同步 | GET | `/alipay/return` | 支付成功同步跳转 |

### 6.6 分类接口

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 获取分类列表 | GET | `/category/list` | 获取所有分类 |

### 6.7 收藏接口

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 添加收藏 | POST | `/favorite/add` | 添加商品到收藏 |
| 删除收藏 | POST | `/favorite/remove` | 取消收藏 |
| 获取收藏列表 | GET | `/favorite/list?userId=xxx` | 获取用户收藏 |

### 6.8 浏览历史接口

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 添加浏览记录 | POST | `/browseHistory/add` | 添加浏览记录 |
| 获取浏览历史 | GET | `/browseHistory/list?userId=xxx` | 获取浏览历史 |

### 6.9 收货地址接口

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 添加地址 | POST | `/receivingAddress/add` | 添加收货地址 |
| 更新地址 | POST | `/receivingAddress/update` | 更新收货地址 |
| 删除地址 | POST | `/receivingAddress/delete` | 删除收货地址 |
| 获取地址列表 | GET | `/receivingAddress/list?userId=xxx` | 获取用户地址列表 |

### 6.10 QQ登录接口

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 获取QQ授权URL | GET | `/qq/auth` | 获取QQ登录链接 |
| QQ回调 | GET | `/qq/callback` | QQ登录回调 |

---

## 7. 配置说明

### 7.1 数据库配置

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/mall?useSSL=false&serverTimezone=Asia/Shanghai
spring.datasource.username=root
spring.datasource.password=admin123
```

### 7.2 Redis配置

```properties
spring.data.redis.host=localhost
spring.data.redis.port=6379
spring.data.redis.password=123456
```

### 7.3 AI配置

- **DeepSeek**：`spring.ai.deepseek.api-key`
- **阿里百炼**：`spring.ai.dashscope.api-key`
- **Qdrant向量数据库**：`spring.ai.vectorstore.qdrant.host=localhost:6334`

### 7.4 支付配置

- **支付宝沙箱**：`alipay.app-id`, `alipay.private-key`, `alipay.alipay-public-key`
- **回调地址**：`alipay.notify-url`, `alipay.return-url`

### 7.5 QQ登录配置

```properties
qq.app-id=1903993948
qq.app-key=X1NaZKrJYZMuDoBK
qq.redirect-uri=http://xxx.natappfree.cc/qq/callback
```

### 7.6 物流配置

```properties
express.customer=51A9B3D4FEEF123E587F925D51B23902
express.key=YGjQoLYO9840
express.secret=214d5ae6fcc24742b33dbda17f749e7c
```

---

## 8. 项目运行方式

### 8.1 环境要求

- **Java**：17+
- **Node.js**：^20.19.0 或 >=22.12.0
- **MySQL**：8.0+
- **Redis**：-
- **Qdrant**：（可选，用于AI向量检索）

### 8.2 后端启动步骤

1. **创建数据库**
   ```sql
   CREATE DATABASE mall CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```

2. **导入数据**
   ```bash
   mysql -u root -p mall < db/mall.sql
   ```

3. **修改配置**
   - 编辑 `server/src/main/resources/application.properties`
   - 修改数据库连接信息、Redis密码等

4. **启动后端**
   ```bash
   cd server
   mvn spring-boot:run
   ```

   或打包后运行：
   ```bash
   mvn clean package
   java -jar target/server-0.0.1-SNAPSHOT.jar
   ```

5. **访问API文档**
   - Swagger UI：http://localhost:7777/swagger-ui.html

### 8.3 前端启动步骤

1. **安装依赖**
   ```bash
   cd mall-client
   npm install
   ```

2. **启动开发服务器**
   ```bash
   npm run dev
   ```

3. **构建生产版本**
   ```bash
   npm run build
   ```

### 8.4 服务端口

| 服务 | 端口 |
|------|------|
| 后端API | 7777 |
| 前端页面 | 5173 |
| MySQL | 3306 |
| Redis | 6379 |
| Qdrant | 6334 |

---

## 9. 安全机制

### 9.1 JWT认证

- 使用JJWT库生成和验证Token
- Token有效期：2小时
- Token存储：前端localStorage

### 9.2 密码加密

- 使用BCrypt算法加密密码
- 工具类：[PasswordUtil.java](file:///d:/hope_school/javaweb/期末项目/server/src/main/java/com/easy/ai/utils/PasswordUtil.java)

### 9.3 登录拦截器

- [LoginInterceptor.java](file:///d:/hope_school/javaweb/期末项目/server/src/main/java/com/easy/ai/interceptors/LoginInterceptor.java)
- 排除公开接口（登录、注册、商品浏览等）
- 通过Authorization头验证Token

### 9.4 CORS配置

- 允许跨域请求
- 配置在[WebConfig.java](file:///d:/hope_school/javaweb/期末项目/server/src/main/java/com/easy/ai/config/WebConfig.java)

---

## 10. 数据流说明

### 10.1 登录流程

```
前端输入邮箱密码 → POST /user/login
                    ↓
后端验证密码 → 生成JWT Token → 返回Token
                    ↓
前端存储Token到localStorage → 后续请求携带Authorization头
```

### 10.2 商品发布流程

```
前端上传图片 → POST /commodity/uploadCommunityImg → 返回图片路径
                    ↓
前端提交商品信息 → POST /commodity/addCommodity
                    ↓
后端保存商品 → 保存相册关联 → 返回成功
```

### 10.3 下单支付流程

```
选择商品 → 选择收货地址 → POST /order/create → 创建订单
                    ↓
POST /alipay/create → 生成支付宝订单 → 返回支付链接/二维码
                    ↓
用户支付 → 支付宝回调 /alipay/notify → 更新订单状态为已支付
                    ↓
卖家发货 → PUT /order/ship → 填写物流单号
                    ↓
买家查询物流 → GET /order/logistics → 返回物流轨迹
```

### 10.4 实时聊天流程

```
前端建立WebSocket连接（携带Token） → /ws?token=xxx
                    ↓
后端解析Token获取userId → 存储用户会话
                    ↓
发送消息 → 后端保存到数据库 → WebSocket推送给目标用户
                    ↓
接收消息 → 更新聊天界面
```

---

## 11. 扩展说明

### 11.1 AI工具扩展

项目采用Spring AI框架，支持通过`@Tool`注解扩展AI可用工具：

```java
@Component
public class MyTool {
    @Tool("工具描述")
    public String querySomething(String param) {
        // 实现逻辑
        return result;
    }
}
```

### 11.2 新增商品分类

1. 在`category`表中插入新分类记录
2. 上传分类图标到`server/upload/category/`目录

### 11.3 自定义AI提示词

编辑`server/src/main/resources/prompts/`目录下的提示词文件：
- `assistant.txt` - AI助手角色定义
- `customer-service.txt` - 客服角色定义