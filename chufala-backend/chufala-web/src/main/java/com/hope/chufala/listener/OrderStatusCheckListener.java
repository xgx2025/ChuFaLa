package com.hope.chufala.listener;

import com.hope.chufala.common.util.DelayMessageProcessor;
import com.hope.chufala.constant.HotelOrderStatus;
import com.hope.chufala.mq.MultiDelayMessage;
import com.hope.chufala.model.entity.HotelOrder;
import com.hope.chufala.service.IHotelOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 订单延迟消息监听器。
 *
 * <p>消费阶梯延迟消息：订单仍未支付且还有下一个延迟时长时重新投递；
 * 阶梯走完（约 30 分钟）仍未支付则取消订单并回补库存。
 * 已支付或已取消的订单直接忽略，不再投递。
 *
 * @author 谢光湘
 */
@Slf4j
@Service
public class OrderStatusCheckListener {

    @Autowired
    private IHotelOrderService hotelOrderService;
    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 处理订单延迟消息（阶梯重投或最终取消）。
     *
     * @param msg 多级延迟消息（消息体为业务订单号）
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value="order.hotel.delay.queue",durable = "true"),
            exchange = @Exchange(value = "order.delay.direct",type = ExchangeTypes.DIRECT,delayed = "true"),
            key = "order.hotel.delay.key"))
    public void listenerOrderDelayMessage(MultiDelayMessage<Long> msg){
        log.info("收到延迟消息：{}",msg);
        HotelOrder order = hotelOrderService.getByOrderId(msg.getData());
        if (order == null || !HotelOrderStatus.UNPAID.equals(order.getOrderStatus())) {
            return;
        }
        //判断是否存在下一个延迟时间
        if(msg.hasNextDelay()){
            //存在重发延迟消息
            Long nextDelay = msg.removeNextDelay();
            rabbitTemplate.convertAndSend("order.delay.direct","order.hotel.delay.key",msg,new DelayMessageProcessor(nextDelay));
            return;
        }
        //不存在下一个延迟时间(说明已过30分钟),取消订单并恢复库存
        try {
            hotelOrderService.cancelDelayOrder(order.getOrderId());
        } catch (RuntimeException e) {
            // 渠道状态不确定时由每分钟的数据库扫描继续重试，避免 MQ 立即重投形成热循环。
            log.warn("延迟关单暂未完成，交由定时任务重试，orderId={}", order.getOrderId(), e);
        }
    }



}
