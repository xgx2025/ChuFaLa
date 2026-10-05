package com.hope.chufala.config;

import com.hope.chufala.interceptor.LoginInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置。
 *
 * <p>三件事：注册登录拦截器并放行白名单（登录/注册/验证码/支付回调/静态资源等）、
 * 把本地目录映射为图片访问路径、把全局异步请求超时设为 240 秒（供 SSE 进度推送
 * 与流式对话使用）。
 *
 * @author 谢光湘
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Autowired
    private LoginInterceptor loginInterceptor;

    /**
     * 注册登录拦截器并声明放行路径。
     *
     * @param registry 拦截器注册表
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor).excludePathPatterns
                ("/druid/**","/auth/login","/auth/register","/auth/refreshToken", "/templates/error", "/auth/sendVerificationCode","/res/hotel/**","/res/user/**","/res/room/**","/alipay/notify","/alipay/return","/**/*.html","/captcha/generate");
    }

    /**
     * 把本地上传目录映射为可访问的静态资源路径。
     *
     * @param registry 资源处理器注册表
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 映射本地路径, 使用 file: 前缀，并确保路径末尾带斜杠
        registry.addResourceHandler("/res/room/image/**").addResourceLocations("file:D:/Service/chufala/room/image/");
        registry.addResourceHandler("/res/hotel/image/**").addResourceLocations("file:D:/Service/chufala/hotel/image/");
        registry.addResourceHandler("/res/user/avatar/**").addResourceLocations("file:D:/Service/chufala/user/avatar/");
    }

    /**
     * 设置全局异步请求超时时间。
     *
     * @param configurer 异步支持配置
     */
    @Override
    public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
        configurer.setDefaultTimeout(240_000);   // 设置全局异步请求超时时间为 120 秒（单位：毫秒）
    }
}

