package com.hope.chufala.task;

import com.hope.chufala.service.IHotelOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExpiredHotelOrderTask {
    private final IHotelOrderService hotelOrderService;

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
