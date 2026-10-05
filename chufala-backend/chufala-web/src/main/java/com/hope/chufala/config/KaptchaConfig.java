package com.hope.chufala.config;

import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

/**
 * 图形验证码配置。
 *
 * <p>4 位纯数字、白底黑字、无边框；由 CaptchaController 调用生成图片，
 * 答案存入 Session 供注册时校验。
 *
 * @author 谢光湘
 */
@Configuration
public class KaptchaConfig {

    /**
     * 构建 Kaptcha 实例。
     *
     * @return 配置好的 DefaultKaptcha
     */
    @Bean
    public DefaultKaptcha defaultKaptcha(){
        DefaultKaptcha kaptcha = new DefaultKaptcha();
        Properties properties = new Properties();
        //验证码文字
        properties.setProperty("kaptcha.textproducer.char.string", "0123456789");
        properties.setProperty("kaptcha.textproducer.char.length", "4");
        //图片样式
        properties.setProperty("kaptcha.border", "no");
        properties.setProperty("kaptcha.background.clear.from", "white");
        properties.setProperty("kaptcha.background.clear.to", "white");
        properties.setProperty("kaptcha.textproducer.font.color", "black");
        properties.setProperty("kaptcha.textproducer.font.size", "40");

        Config config = new Config(properties);
        kaptcha.setConfig(config);
        return kaptcha;
    }

}
