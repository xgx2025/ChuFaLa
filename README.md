# chufala

《出发啦》是一个帮助大家外出旅行的网站。

## 项目结构

```
.
├── chufala-backend/     # 后端：Spring Boot 多模块（Maven）
│   ├── chufala-common/  #   公共模块：工具类、常量、异常、通用配置
│   └── chufala-web/     #   业务模块：控制层、服务层、AI 编排、Mapper
└── chufala-frontend/    # 前端：Vue 3 + Vite + TypeScript
```

---

## 后端（chufala-backend/）

### 1. 准备配置文件

仓库中只保留了脱敏模板，真实配置需要本地生成：

```bash
cd chufala-backend
cp chufala-web/src/main/resources/application.yml.example  chufala-web/src/main/resources/application.yml
cp chufala-common/src/main/resources/application.yml.example chufala-common/src/main/resources/application.yml
cp chufala-web/src/main/resources/mcp-servers-config.json.example chufala-web/src/main/resources/mcp-servers-config.json
```

这三个真实配置文件已被 `.gitignore` 排除，**请勿提交**。

### 2. 设置环境变量

所有敏感值都通过环境变量注入，源码与配置模板中不含任何真实凭据。
也可以直接把真实值写进本地 `application.yml`（该文件已被 `.gitignore` 排除），效果等同。

| 变量名 | 用途 |
| --- | --- |
| `MYSQL_USERNAME` / `MYSQL_PASSWORD` | 数据库账号 |
| `DRUID_USERNAME` / `DRUID_PASSWORD` | Druid 监控台账号 |
| `RABBITMQ_USERNAME` / `RABBITMQ_PASSWORD` | RabbitMQ 账号 |
| `ALIPAY_APP_ID` | 支付宝应用 ID |
| `ALIPAY_SELLER_ID` | 支付宝收款方 PID |
| `ALIPAY_MERCHANT_PRIVATE_KEY` | 支付宝商户私钥 |
| `ALIPAY_PUBLIC_KEY` | 支付宝公钥 |
| `ALIPAY_NOTIFY_URL` | 支付宝服务器异步通知地址，需能从公网访问 |
| `ALIPAY_RETURN_URL` | 支付完成后的浏览器回跳入口，例如本机测试用 `http://127.0.0.1:9010/alipay/return` |
| `ALIPAY_FRONTEND_RETURN_URL` | 回跳入口最终重定向的前端页面，本机测试用 `http://localhost:8881/payment/return` |
| `ALIYUN_OSS_ACCESS_KEY_ID` / `ALIYUN_OSS_ACCESS_KEY_SECRET` | 阿里云 OSS 凭据 |
| `AI_DASHSCOPE_API_KEY` | 阿里云百炼 API Key |
| `ZHIPU_API_KEY` | 智谱 API Key |
| `DEEPSEEK_API_KEY` | DeepSeek API Key |
| `DOUBAO_API_KEY` | 豆包 API Key |
| `AMAP_MAPS_API_KEY` | 高德地图 MCP Key（对应 `amap.api-key`） |
| `SMTP_USER` / `SMTP_PASSWORD` | QQ 邮箱账号与 SMTP 授权码（对应 `email.smtp.user` / `email.smtp.password`） |
| `JWT_ACCESS_TOKEN_SECRET` / `JWT_REFRESH_TOKEN_SECRET` | JWT 签名密钥（对应 `jwt.access-token-secret` / `jwt.refresh-token-secret`） |
| `PRICE_SIGN_SECRET_KEY` | 价格签名 HMAC 密钥（对应 `sign.price-secret-key`） |

Windows（PowerShell）示例：

```powershell
$env:MYSQL_PASSWORD = "你的密码"
$env:JWT_ACCESS_TOKEN_SECRET = "用 openssl rand -base64 32 生成"
```

### 3. 构建运行

```bash
cd chufala-backend
./mvnw -pl chufala-web -am spring-boot:run
```

---

## 前端（chufala-frontend/）

Vue 3 + Vite + TypeScript 项目，默认开发端口 `8881`，并将 `/api` 代理到后端 `http://localhost:9010`。

### 1. 准备环境变量

```bash
cd chufala-frontend
cp .env.example .env.development
```

然后在 `.env.development` 中填入真实 Key。该文件已被 `.gitignore` 排除，**请勿提交**。

| 变量名 | 用途 |
| --- | --- |
| `VITE_BAIDU_MAP_AK` | 百度地图 AK |
| `VITE_AMAP_API_KEY` | 高德地图 Web 端 api-key |
| `VITE_AMAP_SERVICE_KEY` | 高德地图 Web 服务 api-key |
| `VITE_AMAP_SECURITY_CODE` | 高德地图安全密钥 |

客户端变量必须带 `VITE_` 前缀，否则不会被注入到前端代码中。

> 这些 Key 会随前端产物下发到浏览器，本质属于公开信息，因此**必须在地图控制台配置域名 / Referer 白名单**，不要依赖「不提交」来保护它们。

### 2. 安装与运行

```bash
cd chufala-frontend
npm install
npm run dev
```

### 3. HTTPS 本地调试（可选）

仓库不包含证书文件（`*.pem` / `*.key` / `*.csr` / `*.srl` 均被忽略）。如需 HTTPS 调试，请按 `chufala-frontend/HTTPS_CERT_GUIDE.md` 在本地重新生成证书。

### 4. 分享截图服务（可选）

`chufala-frontend/server/` 是一个基于 Puppeteer 的截图服务，监听 `3000`，依赖本地已启动的前端（`8881`）：

```bash
cd chufala-frontend/server
npm install
node index.js
```

---

## 安全约定

- **禁止**将任何真实密钥、私钥、账号密码提交到仓库
- 新增配置项请同步更新对应的 `*.example` 模板（后端 `application.yml.example`、前端 `.env.example`）
- 前端禁止硬编码地图 Key，统一走 `import.meta.env.VITE_*`
- 若怀疑密钥已泄露，请立即在对应平台轮换，改代码无法挽回已泄露的凭据

## 酒店订单状态与库存

- 酒店订单创建时按房型和入住日期逐晚条件扣减 `room_daily_stock.available_stock`，状态为“待支付”。整笔预订处于一个数据库事务中，任一晚库存不足则全部回滚。付款只允许“待支付 → 已支付”，取消只允许“待支付 → 已取消”；取消成功才逐晚回补库存。删除待支付订单会先取消，再软删除。
- 订单创建提交后发送延迟消息。延迟消息在实际满 30 分钟时取消待支付订单；定时任务每分钟扫描一次，兜底处理消息丢失或发送失败。
- 支付宝成功通知需通过签名、应用 ID、收款方 `seller_id`、商户订单号及金额校验。部署时配置 `ALIPAY_SELLER_ID`（收款方 PID）；未配置或不匹配时回调返回 `fail`。支付记录行锁和条件更新保证重复通知只处理一次；支付与超时关单竞争时，仅成功的订单状态变更会产生相应业务效果。
- 浏览器付款后通过 `ALIPAY_RETURN_URL` 回到 `/alipay/return`，再跳转到 `ALIPAY_FRONTEND_RETURN_URL`。本机测试时前者使用 `127.0.0.1`，后者保持用户打开前端时使用的 `localhost:8881`，以保留同一浏览器来源的登录状态。该跳转不修改订单状态；支付结果以异步通知为准。
- **沙箱支付注意事项**：`ALIPAY_NOTIFY_URL` 必须是支付宝服务器可访问、能转发到后端 `POST /alipay/notify` 的公网地址；使用 cpolar 等临时隧道时，每次启动或地址变化后都要核对该地址并重启后端，使新支付请求使用更新后的通知地址。支付页面显示成功或浏览器回到网站，只能说明前台支付流程完成，不能证明异步通知已到达。若酒店订单仍显示“待支付”，先检查隧道是否仍有效、是否收到该订单的 `POST /alipay/notify`，再查看后端回调日志中的验签、`app_id`、`seller_id`、金额和支付记录校验结果。对已经付款但未更新的订单，应先按支付宝交易号核实支付结果并对账，再补发通知或按核实结果处理；不要直接让用户重复付款，也不要仅凭浏览器回跳手动改为“已支付”。
- 若订单已取消，支付记录标为 `REFUND_REQUIRED`，不会重新标记订单为已支付，也不会再次扣减库存。
- 部署新版回调前执行 [支付记录索引迁移](chufala-backend/db/migration/20261005_pay_record_order_index.sql)，让按订单号的行锁查询只扫描该订单的支付记录；迁移保留历史重复的待支付记录。
- **退款待办需要人工处理**：使用 `SELECT id, order_id, money, trade_no, pay_time FROM pay_record WHERE status = 'REFUND_REQUIRED';` 查出记录，在支付宝后台确认并退款后核对交易记录，再将对应记录更新为 `REFUNDED`。当前项目尚未实现自动退款；上线前应补充自动退款、失败重试与对账。

数据库中酒店订单的待付款状态以 `待支付` 为准。逐日库存上线前，停用旧版下单和取消入口，执行 [数据库迁移脚本](chufala-backend/db/migration/20261004_room_daily_stock.sql)。脚本也会把旧数据中的 `未支付` 统一为 `待支付`，并将旧的全局剩余库存还原为房型基准房量，保留现有未取消订单；首次访问入住日期时，系统根据已有订单占用初始化该日可用量。上线后 `room.stock` 表示房型基准房量，实际可用量以 `room_daily_stock` 为准。
