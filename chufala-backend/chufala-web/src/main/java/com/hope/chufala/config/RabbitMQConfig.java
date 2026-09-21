package com.hope.chufala.config;


import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Bean
    public MessageConverter messageConverter(){
        return new Jackson2JsonMessageConverter();
    }



//    //定义交换机名称
//    public static final String EXCHANGE_NAME = "boot.exchange";
//    //定义队列名称
//    public static final String QUEUE_NAME = "boot.queue";
//    //定义路由键
//    public static final String ROUTING_KEY = "boot.routing.key";
//
//    /**
//     * 创建持久化交换机
//     */
//    @Bean
//    public Exchange exchange(){
//        //ExchangerBuilder 提供了流式API来构建交换机
//        return ExchangeBuilder.topicExchange(EXCHANGE_NAME).durable(true).build();
//    }
}
