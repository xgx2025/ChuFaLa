package com.hope.chufala.config;

import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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


   @Bean(name="deepseekChatModel")
   public OpenAiChatModel deepSeekChatModel() {
         OpenAiApi openAiApi = OpenAiApi.builder().apiKey(deepSeekApiKey).baseUrl(deepSeekUrl).build();
         return OpenAiChatModel.builder()
                     .openAiApi(openAiApi)
                     .defaultOptions(OpenAiChatOptions.builder().model(deepSeekModel).build())
                     .build();
   }

   @Bean(name="doubaoChatModel")
   public OpenAiChatModel doubaoChatModel() {
         OpenAiApi openAiApi = OpenAiApi.builder().apiKey(apiKey).baseUrl(url).build();
         return OpenAiChatModel.builder()
                     .openAiApi(openAiApi)
                     .defaultOptions(OpenAiChatOptions.builder().model(model).build())
                     .build();
   }


}
