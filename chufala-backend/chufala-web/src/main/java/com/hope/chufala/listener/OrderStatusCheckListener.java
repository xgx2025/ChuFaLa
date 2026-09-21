package com.hope.chufala.listener;

import com.hope.chufala.common.util.DelayMessageProcessor;
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

@Slf4j
@Service
public class OrderStatusCheckListener {

    @Autowired
    private IHotelOrderService hotelOrderService;
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value="order.hotel.delay.queue",durable = "true"),
            exchange = @Exchange(value = "order.delay.direct",type = ExchangeTypes.DIRECT,delayed = "true"),
            key = "order.hotel.delay.key"))
    public void listenerOrderDelayMessage(MultiDelayMessage<Long> msg){
        log.info("收到延迟消息：{}",msg);
        HotelOrder order = hotelOrderService.getByOrderId(msg.getData());
        if (order == null || "已支付".equals(order.getOrderStatus()) ){
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
        hotelOrderService.cancelDelayOrder(order.getOrderId(),order.getRoomTypeId(),order.getRoomCount());
    }



}
