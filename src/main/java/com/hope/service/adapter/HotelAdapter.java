package com.hope.service.adapter;

import com.hope.domain.entity.HotelOrder;
import com.hope.domain.entity.PayParam;
import com.hope.domain.entity.PayRecord;
import com.hope.mapper.PayRecordMapper;
import com.hope.service.IHotelOrderService;
import com.hope.service.IPayRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class HotelAdapter implements BizAdapter {

    @Autowired
    private IHotelOrderService hotelOrderService;

    @Autowired
    private IPayRecordService payRecordService;

    @Override
    public PayParam buildPayParam(Long orderId) {
        // 1. 查询酒店订单（业务逻辑）
        HotelOrder order = hotelOrderService.getByOrderId(orderId);
        if (order == null) {
            throw new RuntimeException("酒店订单不存在");
        }

        // 2. 转换为统一支付参数（与业务无关）
        PayParam param = new PayParam();
        param.setOrderId(order.getOrderId()); // 商户订单号（带业务前缀）
        param.setMoney(order.getTotalPrice()); // 支付金额
        param.setSubject("酒店预订：" + order.getTitle()); // 订单标题
        param.setBody("房间：" + order.getRoomType() + "，入住时间：" + order.getCheckIn());
        return param;
    }

    @Override
    public void handlePaySuccess(Long orderId, PayRecord payRecord) {
        // 支付成功后处理酒店业务（业务逻辑）
        hotelOrderService.updateStatus(orderId, "已支付");
        //hotelOrderService.sendConfirmSms(orderId); // 发送入住确认短信
        // hotelOrderService.lockRoom(orderId); // 锁定房间资源
    }
}