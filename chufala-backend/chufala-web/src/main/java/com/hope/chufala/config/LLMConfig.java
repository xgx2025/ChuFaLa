package com.hope.chufala.config;

import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 大模型接入配置。
 *
 * <p>以 OpenAI 兼容协议接入两家服务：deepseek（llm.* 之外单独一套配置）与
 * doubao（豆包，使用 llm.*）；密钥、地址与模型名均来自 application.yml。
 *
 * @author 谢光湘
 */
@Configuration
public class LLMConfig {
   @Value("${llm.api-key}")
   private String apiKey;
   @Value("${llm.model}")
   private String model;
   @Value("${llm.url}")
   private String url;

   @Value("${deepseek.api-key}")
   private String deepSeekApiKey;
   @Value("${deepseek.url}")
   private String deepSeekUrl;
   @Value("${deepseek.model}")
   private String deepSeekModel;


   /**
    * DeepSeek 模型（Bean 名 deepseekChatModel）。
    *
    * @return OpenAI 兼容模型实例
    */
   @Bean(name="deepseekChatModel")
   public OpenAiChatModel deepSeekChatModel() {
         OpenAiApi openAiApi = OpenAiApi.builder().apiKey(deepSeekApiKey).baseUrl(deepSeekUrl).build();
         return OpenAiChatModel.builder()
                     .openAiApi(openAiApi)
                     .defaultOptions(OpenAiChatOptions.builder().model(deepSeekModel).build())
                     .build();
   }

   /**
    * 豆包模型（Bean 名 doubaoChatModel，用于图片理解）。
    *
    * @return OpenAI 兼容模型实例
    */
   @Bean(name="doubaoChatModel")
   public OpenAiChatModel doubaoChatModel() {
         OpenAiApi openAiApi = OpenAiApi.builder().apiKey(apiKey).baseUrl(url).build();
         return OpenAiChatModel.builder()
                     .openAiApi(openAiApi)
                     .defaultOptions(OpenAiChatOptions.builder().model(model).build())
                     .build();
   }


}
