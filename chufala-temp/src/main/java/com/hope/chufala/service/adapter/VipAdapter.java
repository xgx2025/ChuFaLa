package com.hope.chufala.service.adapter;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.hope.chufala.domain.entity.*;
import com.hope.chufala.mapper.UserMapper;
import com.hope.chufala.mapper.VipPaymentRecordMapper;
import com.hope.chufala.service.IHotelOrderService;
import com.hope.chufala.service.IMembershipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VipAdapter implements BizAdapter{

    @Autowired
    private IMembershipService membershipService;
    @Autowired
    private VipPaymentRecordMapper vipPaymentRecordMapper;
    @Autowired
    private UserMapper userMapper;

    @Override
    public PayParam buildPayParam(Long orderId) {
        // 1. 查询酒店订单（业务逻辑）
        VipPaymentRecord order = membershipService.getRecordById(orderId);
        if (order == null) {
            throw new RuntimeException("vip订阅订单不存在");
        }

        // 2. 转换为统一支付参数（与业务无关）
        PayParam param = new PayParam();
        param.setOrderId(order.getOutTradeNo()); // 商户订单号（带业务前缀）
        param.setMoney(order.getTotalAmount()); // 支付金额
        param.setSubject("会员订阅"); // 订单标题
        param.setBody("超级会员订阅");
        return param;
    }

    @Transactional
    @Override
    public void handlePaySuccess(Long orderId, PayRecord payRecord) {
        // 支付成功后处理会员业务
        UpdateWrapper<VipPaymentRecord> updateWrapper = new UpdateWrapper<>();
        updateWrapper.set("pay_status",1).eq("out_trade_no",orderId);
        vipPaymentRecordMapper.update(null, updateWrapper);
        UpdateWrapper<User> update = new UpdateWrapper<>();
        update.eq("id",payRecord.getUserId()).set("vip",1);
        userMapper.update(null, update);
    }
}
