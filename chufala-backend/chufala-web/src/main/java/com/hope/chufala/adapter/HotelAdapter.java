package com.hope.chufala.adapter;

import com.hope.chufala.model.entity.HotelOrder;
import com.hope.chufala.constant.HotelOrderStatus;
import com.hope.chufala.common.exception.ResourceNotFoundException;
import com.hope.chufala.model.dto.PayParamDTO;
import com.hope.chufala.model.entity.PayRecord;
import com.hope.chufala.service.IHotelOrderService;
import com.hope.chufala.service.IPayRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * 酒店业务适配器。
 *
 * <p>把酒店订单转换为统一支付参数，并在支付成功后推进订单状态；发起支付前会校验
 * 订单归属、状态，以及 30 分钟支付时限。
 *
 * @author 谢光湘
 */
@Service
public class HotelAdapter implements BizAdapter {

    @Autowired
    private IHotelOrderService hotelOrderService;

    @Autowired
    private IPayRecordService payRecordService;

    /**
     * 由酒店订单构建统一支付参数。
     *
     * @param orderId 业务订单 ID
     * @param userId  下单用户 ID
     * @return 统一支付参数
     */
    @Override
    public PayParamDTO buildPayParam(Long orderId, Long userId) {
        // 1. 查询酒店订单（业务逻辑）
        HotelOrder order = hotelOrderService.getByOrderIdAndUserId(orderId, userId);
        if (order == null) {
            throw new ResourceNotFoundException("酒店订单不存在");
        }
        if (!HotelOrderStatus.UNPAID.equals(order.getOrderStatus())
                || (order.getBookTime() != null
                && !order.getBookTime().plusMinutes(30).isAfter(LocalDateTime.now(ZoneId.of("Asia/Shanghai"))))) {
            throw new IllegalArgumentException("订单已取消、已支付或已超时，不能发起支付");
        }

        // 2. 转换为统一支付参数（与业务无关）
        PayParamDTO param = new PayParamDTO();
        param.setOrderId(order.getOrderId()); // 商户订单号（带业务前缀）
        param.setMoney(BigDecimal.valueOf(order.getTotalPrice())); // 支付金额
        param.setSubject("酒店预订：" + order.getTitle()); // 订单标题
        param.setBody("房间：" + order.getRoomType() + "，入住时间：" + order.getCheckIn());
        if (order.getBookTime() == null) {
            throw new IllegalStateException("酒店订单缺少下单时间，不能发起支付");
        }
        param.setExpireTime(order.getBookTime().plusMinutes(30));
        return param;
    }

    /**
     * 支付成功后把酒店订单标记为已支付。
     *
     * @param orderId   业务订单 ID
     * @param payRecord 支付记录
     */
    @Override
    public void handlePaySuccess(Long orderId, PayRecord payRecord) {
        // 支付成功后处理酒店业务（业务逻辑）
        hotelOrderService.markOrderPaid(orderId);
        //hotelOrderService.sendConfirmSms(orderId); // 发送入住确认短信
        // hotelOrderService.lockRoom(orderId); // 锁定房间资源
    }
}
