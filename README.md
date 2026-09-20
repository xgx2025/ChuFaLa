# chufala

《出发啦》是一个帮助大家外出旅行的网站。

## 项目结构

```
.
├── chufala-backend/     # 后端：Spring Boot 多模块（Maven）
│   ├── chufala-common/  #   公共模块：工具类、常量、异常、通用配置
│   └── chufala-temp/    #   业务模块：控制层、服务层、AI 编排、Mapper
└── chufala-frontend/    # 前端：Vue 3 + Vite + TypeScript
```

---

## 后端（chufala-backend/）

### 1. 准备配置文件

仓库中只保留了脱敏模板，真实配置需要本地生成：

```bash
cd chufala-backend
cp chufala-temp/src/main/resources/application.yml.example  chufala-temp/src/main/resources/application.yml
cp chufala-common/src/main/resources/application.yml.example chufala-common/src/main/resources/application.yml
cp chufala-temp/src/main/resources/mcp-servers-config.json.example chufala-temp/src/main/resources/mcp-servers-config.json
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
| `ALIPAY_MERCHANT_PRIVATE_KEY` | 支付宝商户私钥 |
| `ALIPAY_PUBLIC_KEY` | 支付宝公钥 |
| `ALIPAY_NOTIFY_URL` / `ALIPAY_RETURN_URL` | 支付回调地址 |
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
./mvnw -pl chufala-temp -am spring-boot:run
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
