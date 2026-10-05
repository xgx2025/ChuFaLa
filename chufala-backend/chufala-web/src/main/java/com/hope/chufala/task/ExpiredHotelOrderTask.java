package com.hope.chufala.task;

import com.hope.chufala.service.IHotelOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 超时未支付订单清理任务。
 *
 * <p>每 60 秒扫描一次超时订单并取消（回补库存），作为延迟消息的兜底：
 * 即使 MQ 消息丢失，订单也能被回收。单个订单失败不影响其余订单。
 *
 * @author 谢光湘
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExpiredHotelOrderTask {
    private final IHotelOrderService hotelOrderService;

    /**
     * 扫描并取消超时未支付订单（单次最多 100 条）。
     */
    @Scheduled(fixedDelay = 60_000)
    public void cancelExpiredOrders() {
        for (Long orderId : hotelOrderService.getExpiredUnpaidOrderIds(100)) {
            try {
                hotelOrderService.cancelDelayOrder(orderId);
            } catch (Exception e) {
                log.error("超时订单取消失败，orderId={}", orderId, e);
            }
        }
    }
}
