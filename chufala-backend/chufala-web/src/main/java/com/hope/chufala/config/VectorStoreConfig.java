package com.hope.chufala.config;

import io.milvus.client.MilvusServiceClient;
import io.milvus.param.ConnectParam;
import io.milvus.param.IndexType;
import io.milvus.param.MetricType;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.TokenCountBatchingStrategy;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.milvus.MilvusVectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Milvus 向量库配置。
 *
 * <p>创建 Milvus 客户端与 VectorStore（集合 test_vector_store、IVF_FLAT 索引、
 * COSINE 距离、按 token 数分批），嵌入模型使用 dashscopeEmbeddingModel；
 * 供 RAGController 等检索场景使用。
 *
 * @author 谢光湘
 */
@Configuration
public class VectorStoreConfig {
    /**
     * 创建 Milvus 客户端。
     *
     * @param host 主机
     * @param port 端口
     * @return Milvus 客户端
     */
    @Bean
    public MilvusServiceClient milvusClient(
            @Value("${spring.ai.vectorstore.milvus.client.host}") String host,
            @Value("${spring.ai.vectorstore.milvus.client.port}") int port) {
        ConnectParam connectParam = ConnectParam.newBuilder()
                .withHost(host)
                .withPort(port)
                // 如果有鉴权，可在此处添加 .withAuthorization("username", "password")
                .build();
        return new MilvusServiceClient(connectParam);
    }

    /**
     * 创建向量库（Milvus 实现）。
     *
     * @param milvusClient    Milvus 客户端
     * @param embeddingModel  嵌入模型
     * @return 向量库
     */
    @Bean
    public VectorStore vectorStore(MilvusServiceClient milvusClient, @Qualifier("dashscopeEmbeddingModel") EmbeddingModel embeddingModel) {
        return MilvusVectorStore.builder(milvusClient, embeddingModel)
                .collectionName("test_vector_store")
                .databaseName("default")
                .indexType(IndexType.IVF_FLAT)
                .metricType(MetricType.COSINE)
                .batchingStrategy(new TokenCountBatchingStrategy())
                .initializeSchema(true)
                .build();
    }
}
