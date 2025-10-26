package com.hope.utils;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

/**
 * 邮件发送工具类
 * 用于发送各类邮件消息
 */
public class EmailUtil {
    // 邮件服务器配置
    private static final String SMTP_HOST = "smtp.qq.com";
    private static final String SMTP_PORT = "465";
    private static final String USERNAME = "3757549181@qq.com";
    private static final String PASSWORD = "plcbcdatallvdcbe"; // SMTP授权码

    /**
     * 发送邮件
     * @param to 收件人邮箱地址
     * @param subject 邮件主题
     * @param content 邮件内容
     * @throws MessagingException 邮件发送异常
     */
    public static void sendEmail(String to, String subject, String content) throws MessagingException {
        // 配置邮件服务器属性
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);
        props.put("mail.smtp.ssl.enable", "true");
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");
        props.put("mail.smtp.timeout", "5000"); // 超时时间
        // 创建认证器
        Authenticator authenticator = new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(USERNAME, PASSWORD);
            }
        };

        // 创建邮件会话
        Session mailSession = Session.getInstance(props, authenticator);
        mailSession.setDebug(true); // 启用调试模式，便于排查问题

        // 创建邮件消息
        MimeMessage message = new MimeMessage(mailSession);
        message.setFrom(new InternetAddress(USERNAME));
        message.setRecipient(MimeMessage.RecipientType.TO, new InternetAddress(to));
        message.setSubject(subject);
        message.setContent(content, "text/html;charset=UTF-8");

        // 发送邮件
        Transport.send(message);
    }
}
