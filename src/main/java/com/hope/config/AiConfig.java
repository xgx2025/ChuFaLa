package com.hope.config;

import com.hope.tools.AttractionTools;
import com.hope.tools.MapTools;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.ai.zhipuai.ZhiPuAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class AiConfig {
    @Resource
    public AttractionTools attractionTools;
    @Resource
    public MapTools mapTools;


    @Bean(name = "zhipu")
    public ChatClient zhipuAiClient(ZhiPuAiChatModel chatModel, ChatMemory chatMemory){
        return ChatClient.builder(chatModel)
                .defaultAdvisors(MessageChatMemoryAdvisor
                        .builder(chatMemory)
                        .build())
                .defaultSystem("你是一位旅游行程规划师，请严格按照 JSON 格式返回结果，不要包含任何解释（报错解释）、前缀、后缀、Markdown 代码块（如 ```json）、注释或额外文字。返回的 JSON 必须符合 TravelItinerary 类的结构。只输出 JSON，其他什么都不要！\n" +
                        "景点和酒店信息来自数据库。对于景点信息AttractionInfo，请填充一下description、tip、position、distance、drivingTime字段，position、distance(与下一个景点的距离)、drivingTime(驾车到下一个景点所需时间)可以通过高德服务工具查询，description中不要有引号（会引起编码解析不了的问题）。天气请转化成这里的词汇[晴天、多云、阴天、小雨、中雨、大雨]。对于酒店信息HotelInfo，请填充一下downtownDistance（到市中心的距离）字段。规划完后，请计算一下预算，并填充BudgetSummary中的字段。\n" +
                        "只输出 JSON，其他什么都不要！只输出 JSON，其他什么都不要！只输出 JSON，其他什么都不要！")
                .defaultTools(attractionTools,mapTools)
                .build();
    }

    @Bean
    public ChatMemory chatMemory(JdbcChatMemoryRepository jdbcChatMemoryRepository){
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(jdbcChatMemoryRepository)
                .maxMessages(50)
                .build();
    }
}
