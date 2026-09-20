package com.hope.chufala.common.util;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.Properties;

/**
 * 邮件发送工具类
 * 用于发送各类邮件消息
 *
 * 邮件服务器配置统一由 application.yml 的 email.smtp.* 提供，不在源码中硬编码
 */
@Component
public class EmailUtils {

    @Value("${email.smtp.host:smtp.qq.com}")
    private String smtpHost;

    @Value("${email.smtp.port:465}")
    private String smtpPort;

    @Value("${email.smtp.user}")
    private String username;

    @Value("${email.smtp.password}")
    private String password;

    /**
     * 发送邮件
     * @param to 收件人邮箱地址
     * @param subject 邮件主题
     * @param content 邮件内容
     * @throws MessagingException 邮件发送异常
     */
    public void sendEmail(String to, String subject, String content) throws MessagingException {
        // 配置邮件服务器属性
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.host", smtpHost);
        props.put("mail.smtp.port", smtpPort);
        props.put("mail.smtp.ssl.enable", "true");
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");
        props.put("mail.smtp.timeout", "5000"); // 超时时间
        // 创建认证器
        Authenticator authenticator = new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        };

        // 创建邮件会话
        Session mailSession = Session.getInstance(props, authenticator);
        mailSession.setDebug(false); // 生产环境关闭调试

        // 创建邮件消息
        MimeMessage message = new MimeMessage(mailSession);
        message.setFrom(new InternetAddress(username));
        message.setRecipient(MimeMessage.RecipientType.TO, new InternetAddress(to));
        message.setSubject(subject);
        message.setContent(content, "text/html;charset=UTF-8");

        // 发送邮件
        Transport.send(message);
    }
}
