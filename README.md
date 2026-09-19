# chufala

《出发啦》是一个帮助大家外出旅行的网站

## 项目结构

```
chufala-backend/
├── chufala-common/    # 公共模块：工具类、常量、异常、通用配置
└── chufala-temp/      # 业务模块：控制层、服务层、AI 编排、Mapper
```

## 本地启动

### 1. 准备配置文件

仓库中只保留了脱敏模板，真实配置需要本地生成：

```bash
cp chufala-temp/src/main/resources/application.yml.example  chufala-temp/src/main/resources/application.yml
cp chufala-common/src/main/resources/application.yml.example chufala-common/src/main/resources/application.yml
cp chufala-temp/src/main/resources/mcp-servers-config.json.example chufala-temp/src/main/resources/mcp-servers-config.json
```

这三个真实配置文件已被 `.gitignore` 排除，**请勿提交**。

### 2. 设置环境变量

所有敏感值都通过环境变量注入，源码与配置模板中不含任何真实凭据。

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
| `AMAP_MAPS_API_KEY` | 高德地图 MCP Key |
| `SMTP_USER` / `SMTP_PASSWORD` | QQ 邮箱账号与 SMTP 授权码 |
| `JWT_ACCESS_TOKEN_SECRET` / `JWT_REFRESH_TOKEN_SECRET` | JWT 签名密钥 |
| `PRICE_SIGN_SECRET_KEY` | 价格签名 HMAC 密钥 |
| `URL_SIGN_SECRET_KEY` | URL 签名 HMAC 密钥 |

Windows（PowerShell）示例：

```powershell
$env:MYSQL_PASSWORD = "你的密码"
$env:JWT_ACCESS_TOKEN_SECRET = "用 openssl rand -base64 32 生成"
```

### 3. 构建运行

```bash
./mvnw -pl chufala-temp -am spring-boot:run
```

## 安全约定

- **禁止**将任何真实密钥、私钥、账号密码提交到仓库
- 新增配置项请同步更新对应的 `*.example` 模板
- 若怀疑密钥已泄露，请立即在对应平台轮换，改代码无法挽回已泄露的凭据
