package com.hope.chufala.common.util;


import lombok.RequiredArgsConstructor;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;

/**
 * 延迟消息处理器。
 *
 * <p>通过设置消息的 delay 属性，让 RabbitMQ 延迟交换机在指定毫秒数后再投递；
 * 是订单阶梯延迟消息（MultiDelayMessage）的实现基础。
 *
 * @author 谢光湘
 */
@RequiredArgsConstructor
public class DelayMessageProcessor implements MessagePostProcessor {

    /** 延迟毫秒数 */
    private final long delay;

    /**
     * 给消息打上延迟属性。
     *
     * @param message 原始消息
     * @return 附加了延迟属性的消息
     * @throws AmqpException 处理失败
     */
    @Override
    public Message postProcessMessage(Message message) throws AmqpException {
        message.getMessageProperties().setDelayLong(delay);
        return message;
    }
}
