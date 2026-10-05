package com.hope.chufala;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;


/**
 * 应用启动类。
 *
 * <p>开启 RabbitMQ、异步与定时任务能力；同时排除三个自动配置：DashScope 的语音
 * 相关配置（项目未使用），以及 Milvus 向量库自动配置（改由 VectorStoreConfig
 * 手动装配，避免自动配置与手动配置重复建连）。
 *
 * @author 谢光湘
 */
@SpringBootApplication(exclude = {
        com.alibaba.cloud.ai.autoconfigure.dashscope.DashScopeAudioSpeechAutoConfiguration.class,
        com.alibaba.cloud.ai.autoconfigure.dashscope.DashScopeAudioTranscriptionAutoConfiguration.class,
        org.springframework.ai.vectorstore.milvus.autoconfigure.MilvusVectorStoreAutoConfiguration.class
})
@EnableRabbit
@EnableAsync
@EnableScheduling
public class ChufalaApplication {
    public static void main(String[] args) {
        SpringApplication.run(ChufalaApplication.class, args);
    }

}


