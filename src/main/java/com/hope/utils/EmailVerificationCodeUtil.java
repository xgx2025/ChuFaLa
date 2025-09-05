package com.hope.utils;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import javax.mail.Authenticator;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import java.io.UnsupportedEncodingException;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 邮件验证码服务类（Redis存储版）
 * 负责生成、发送、验证和管理验证码
 */
@Component
public class EmailVerificationCodeUtil {
    // Redis存储前缀
    private static final String REDIS_KEY_PREFIX = "email:code:";
    // 验证码有效期（分钟）
    private static final long CODE_EXPIRE_MINUTES = 10;
    // 验证码长度
    private static final int CODE_LENGTH = 6;
    // 发送间隔限制（秒）- 防止频繁发送
    private static final long SEND_INTERVAL_SECONDS = 60;

    // 邮件服务器配置
    private static final String SMTP_HOST = "smtp.qq.com";
    private static final String SMTP_PORT = "465";
    private static final String SMTP_USER = "3757549181@qq.com";
    private static final String SMTP_PASSWORD = "plcbcdatallvdcbe"; // QQ邮箱授权码
    private static final String SENDER_NAME = "出发啦";

    private final StringRedisTemplate stringRedisTemplate;

    // 构造注入Redis模板
    public EmailVerificationCodeUtil(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    /**
     * 生成并发送验证码（添加频率控制）
     * @param email 目标邮箱
     * @return 包含验证码和状态的结果对象
     * @throws MessagingException 邮件发送异常
     */
    public Result generateAndSendCode(String email) throws MessagingException, UnsupportedEncodingException {
        String redisKey = REDIS_KEY_PREFIX + email;

        // 检查是否存在未过期的验证码且处于冷却期
        Object lastSendTimeObject = stringRedisTemplate.opsForHash().get(redisKey, "lastSendTime");
        if (lastSendTimeObject != null) {
            String lastSendTimeStr =  (String) lastSendTimeObject;
            long lastSendTime = Long.parseLong(lastSendTimeStr);
            long currentTime = System.currentTimeMillis();

            if (currentTime - lastSendTime < TimeUnit.SECONDS.toMillis(SEND_INTERVAL_SECONDS)) {
                long remaining = (TimeUnit.SECONDS.toMillis(SEND_INTERVAL_SECONDS) - (currentTime - lastSendTime))
                        / 1000;
                return new Result(false,  "请不要频繁发送验证码，剩余" + remaining + "秒");
            }
        }

        // 生成验证码
        String code;
        // 检查是否存在未过期的验证码
        String existingCode = (String) stringRedisTemplate.opsForHash().get(redisKey, "code");
        if (existingCode != null) {
            code = existingCode;
        } else {
            code = generateRandomCode();
        }

        // 存储验证码信息到Redis
        long currentTime = System.currentTimeMillis();
        stringRedisTemplate.opsForHash().put(redisKey, "code", code);
        stringRedisTemplate.opsForHash().put(redisKey, "createTime", String.valueOf(currentTime));
        stringRedisTemplate.opsForHash().put(redisKey, "lastSendTime", String.valueOf(currentTime));
        // 设置过期时间
        stringRedisTemplate.expire(redisKey, CODE_EXPIRE_MINUTES, TimeUnit.MINUTES);

        // 发送邮件
        sendEmail(email, code);

        return new Result(true,  "验证码发送成功");
    }

    /**
     * 验证验证码
     * @param email 邮箱
     * @param code 待验证的验证码
     * @return 验证结果
     */
    public boolean verifyCode(String email, String code) {
        if (email == null || code == null) {
            return false;
        }

        String redisKey = REDIS_KEY_PREFIX + email;
        String storedCode = (String) stringRedisTemplate.opsForHash().get(redisKey, "code");

        // 验证码不存在或不匹配
        if (storedCode == null || !storedCode.equals(code)) {
            return false;
        }

        // 验证成功后移除验证码，防止重复使用
        stringRedisTemplate.delete(redisKey);
        return true;
    }

    /**
     * 移除指定邮箱的验证码
     * @param email 邮箱
     */
    public void removeCode(String email) {
        stringRedisTemplate.delete(REDIS_KEY_PREFIX + email);
    }

    /**
     * 生成随机验证码
     * @return 数字验证码
     */
    private String generateRandomCode() {
        Random rand = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(rand.nextInt(10));
        }
        return code.toString();
    }

    /**
     * 发送邮件
     * @param toEmail 接收邮箱
     * @param code 验证码
     * @throws MessagingException 邮件发送异常
     */
    private void sendEmail(String toEmail, String code) throws MessagingException, UnsupportedEncodingException {
        // 配置邮件服务器
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);
        props.put("mail.smtp.ssl.enable", "true");
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");
        props.put("mail.smtp.timeout", "5000"); // 超时时间

        // 创建会话
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SMTP_USER, SMTP_PASSWORD);
            }
        });
        session.setDebug(false); // 生产环境关闭调试

        // 创建邮件消息
       MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(SMTP_USER, SENDER_NAME, "UTF-8"));
        message.setRecipient(MimeMessage.RecipientType.TO, new InternetAddress(toEmail));
        message.setSubject(SENDER_NAME + "验证码", "UTF-8");
        message.setSentDate(new java.util.Date());

        // 邮件内容
        String content = String.format(
                "【%s】您的验证码是：<strong>%s</strong><br/>" +
                        "该验证码10分钟内有效，请尽快完成验证。<br/>" +
                        "如非本人操作，请忽略此邮件。",
                SENDER_NAME, code
        );
        message.setContent(content, "text/html;charset=UTF-8");

        // 发送邮件
        Transport.send(message);
    }

    /**
     * 验证码操作结果封装类
     */
    public static class Result {
        private boolean success;    // 操作是否成功
        private String message;     // 提示信息

        public Result(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        // Getter方法
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
    }
}
