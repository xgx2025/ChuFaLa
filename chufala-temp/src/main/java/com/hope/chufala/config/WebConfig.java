package com.hope.chufala.config;

import com.hope.chufala.interceptor.LoginInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Autowired
    private LoginInterceptor loginInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor).excludePathPatterns
                ("/druid/**","/auth/login","/auth/register","/auth/refreshToken", "/templates/error", "/auth/sendVerificationCode","/res/hotel/**","/res/user/**","/res/room/**","/alipay/notify","/**/*.html","/performance","/captcha/generate","/agent/progress/**","/rag/**");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 映射本地路径, 使用 file: 前缀，并确保路径末尾带斜杠
        registry.addResourceHandler("/res/room/image/**").addResourceLocations("file:D:/Service/chufala/room/image/");
        registry.addResourceHandler("/res/hotel/image/**").addResourceLocations("file:D:/Service/chufala/hotel/image/");
        registry.addResourceHandler("/res/user/avatar/**").addResourceLocations("file:D:/Service/chufala/user/avatar/");
    }

    @Override
    public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
        configurer.setDefaultTimeout(240_000);   // 设置全局异步请求超时时间为 120 秒（单位：毫秒）
    }
}

