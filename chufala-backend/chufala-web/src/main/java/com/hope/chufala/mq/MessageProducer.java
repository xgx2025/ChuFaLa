package com.hope.chufala.mq;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * RabbitMQ 通用消息生产者。
 *
 * @author 谢光湘
 */
@Service
public class MessageProducer {

    @Autowired
    private RabbitTemplate rabbitTemplate;
    /**
     * 发送消息到指定交换机与路由键。
     *
     * @param exchange   交换机
     * @param routingKey 路由键
     * @param message    消息体
     */
    public void sendMessage(String exchange, String routingKey, Object message) {
        rabbitTemplate.convertAndSend(exchange, routingKey, message);
    }
}
