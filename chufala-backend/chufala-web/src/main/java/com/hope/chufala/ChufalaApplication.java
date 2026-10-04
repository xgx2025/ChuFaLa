package com.hope.chufala;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;


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


