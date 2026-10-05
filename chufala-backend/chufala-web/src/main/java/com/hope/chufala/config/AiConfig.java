package com.hope.chufala.config;

import com.hope.chufala.agent.tool.AttractionTools;
import com.hope.chufala.agent.tool.MapTools;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.zhipuai.ZhiPuAiChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * AI 客户端与对话记忆配置。
 *
 * <p>注册四个 ChatClient，Bean 名即模型标识，供 AgentController 按名取用：
 * zhipu / deepseek / qwen / doubao（后两者中的 deepseek、doubao 走 OpenAI 兼容协议）。
 * 同时装配基于 JDBC 的窗口式对话记忆，最多保留 15 条消息。
 *
 * @author 谢光湘
 */
@Configuration
public class AiConfig {
    @Resource
    public AttractionTools attractionTools;
    @Resource
    public MapTools mapTools;

    /**
     * 聊天记忆建议器
     *
     * @param chatMemory 对话记忆
     * @return 记忆建议器
     */
    @Bean
    public MessageChatMemoryAdvisor messageChatMemoryAdvisor(ChatMemory chatMemory){
        return MessageChatMemoryAdvisor
                .builder(chatMemory)
                .build();
    }

    /**
     * 智谱 ChatClient（Bean 名 zhipu）。
     *
     * @param chatModel 智谱模型
     * @return ChatClient
     */
    @Bean(name = "zhipu")
    public ChatClient zhipuAiClient(ZhiPuAiChatModel chatModel){
        return ChatClient.builder(chatModel)
                .defaultAdvisors(SimpleLoggerAdvisor.builder().build())
                .build();
    }


    /**
     * DeepSeek ChatClient（Bean 名 deepseek，仅 VIP 可用）。
     *
     * @param chatModel 由 LLMConfig 提供的 OpenAI 兼容模型
     * @return ChatClient
     */
    @Bean(name = "deepseek")
    public ChatClient deepSeekClient(@Qualifier("deepseekChatModel") OpenAiChatModel chatModel){
        return ChatClient.builder(chatModel)
                .defaultAdvisors(SimpleLoggerAdvisor.builder().build())
                .build();
    }

    /**
     * 通义千问 ChatClient（Bean 名 qwen，默认模型）。
     *
     * @param chatModel Ollama 模型
     * @return ChatClient
     */
    @Bean(name="qwen")
    public ChatClient qwenClient(OllamaChatModel chatModel){
        return ChatClient.builder(chatModel)
                .defaultAdvisors(SimpleLoggerAdvisor.builder().build())
                .build();
    }

    /**
     * 豆包 ChatClient（Bean 名 doubao，用于图片多模态理解）。
     *
     * @param chatModel 由 LLMConfig 提供的 OpenAI 兼容模型
     * @return ChatClient
     */
    @Bean(name="doubao")
    public ChatClient doubaoClient(@Qualifier("doubaoChatModel")OpenAiChatModel chatModel){
        return ChatClient.builder(chatModel)
                .defaultAdvisors(SimpleLoggerAdvisor.builder().build())
                .build();
    }

    /**
     * 聊天记忆
     *
     * @param jdbcChatMemoryRepository JDBC 记忆仓储
     * @return 窗口式对话记忆（最多 15 条）
     */
    @Bean
    public ChatMemory chatMemory(JdbcChatMemoryRepository jdbcChatMemoryRepository){
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(jdbcChatMemoryRepository)
                .maxMessages(15)
                .build();
    }

}
