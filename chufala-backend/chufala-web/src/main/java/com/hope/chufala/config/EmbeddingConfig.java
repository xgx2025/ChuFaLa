package com.hope.chufala.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 向量嵌入模型配置。
 *
 * <p>当前未启用：原 qwen-text-v4 的 EmbeddingModel 定义已被注释，
 * 向量能力实际由 VectorStoreConfig 承担。
 *
 * @author 谢光湘
 */
@Configuration
public class EmbeddingConfig {

//    @Bean(name="qwen-text-v4")
//    public EmbeddingModel qwenEmbeddingModel(){
//        return EmbeddingModel.builder()
//                .modelName("qwen-text-v4")
//                .build();
//    }
}
