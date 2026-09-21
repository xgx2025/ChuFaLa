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


@Configuration
public class AiConfig {
    @Resource
    public AttractionTools attractionTools;
    @Resource
    public MapTools mapTools;

    /**
     * 聊天记忆建议器
     * @param chatMemory
     * @return
     */
    @Bean
    public MessageChatMemoryAdvisor messageChatMemoryAdvisor(ChatMemory chatMemory){
        return MessageChatMemoryAdvisor
                .builder(chatMemory)
                .build();
    }

    @Bean(name = "zhipu")
    public ChatClient zhipuAiClient(ZhiPuAiChatModel chatModel){
        return ChatClient.builder(chatModel)
                .defaultAdvisors(SimpleLoggerAdvisor.builder().build())
                .build();
    }


    @Bean(name = "deepseek")
    public ChatClient deepSeekClient(@Qualifier("deepseekChatModel") OpenAiChatModel chatModel){
        return ChatClient.builder(chatModel)
                .defaultAdvisors(SimpleLoggerAdvisor.builder().build())
                .build();
    }

    @Bean(name="qwen")
    public ChatClient qwenClient(OllamaChatModel chatModel){
        return ChatClient.builder(chatModel)
                .defaultAdvisors(SimpleLoggerAdvisor.builder().build())
                .build();
    }

    @Bean(name="doubao")
    public ChatClient doubaoClient(@Qualifier("doubaoChatModel")OpenAiChatModel chatModel){
        return ChatClient.builder(chatModel)
                .defaultAdvisors(SimpleLoggerAdvisor.builder().build())
                .build();
    }

    /**
     * 聊天记忆
     * @param jdbcChatMemoryRepository
     * @return
     */
    @Bean
    public ChatMemory chatMemory(JdbcChatMemoryRepository jdbcChatMemoryRepository){
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(jdbcChatMemoryRepository)
                .maxMessages(15)
                .build();
    }

}
