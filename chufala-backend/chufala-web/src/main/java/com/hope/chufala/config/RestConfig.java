package com.hope.chufala.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * REST 客户端配置。
 *
 * <p>提供一个默认的 RestTemplate Bean，供外部 HTTP 调用使用。
 *
 * @author 谢光湘
 */
@Configuration
public class RestConfig {
    /**
     * 创建 RestTemplate。
     *
     * @return RestTemplate
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
