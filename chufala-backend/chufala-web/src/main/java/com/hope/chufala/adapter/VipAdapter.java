package com.hope.chufala.adapter;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.hope.chufala.model.entity.*;
import com.hope.chufala.common.exception.ResourceNotFoundException;
import com.hope.chufala.mapper.UserMapper;
import com.hope.chufala.mapper.VipPaymentRecordMapper;
import com.hope.chufala.service.IMembershipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.hope.chufala.model.dto.PayParamDTO;

/**
 * 会员业务适配器。
 *
 * <p>把会员订单转换为统一支付参数；支付成功后在同一事务内把支付记录置为成功，
 * 并把用户标记为 VIP。
 *
 * @author 谢光湘
 */
@Service
public class VipAdapter implements BizAdapter{

    @Autowired
    private IMembershipService membershipService;
    @Autowired
    private VipPaymentRecordMapper vipPaymentRecordMapper;
    @Autowired
    private UserMapper userMapper;

    /**
     * 由会员订单构建统一支付参数。
     *
     * @param orderId 业务订单 ID
     * @param userId  下单用户 ID
     * @return 统一支付参数
     */
    @Override
    public PayParamDTO buildPayParam(Long orderId, Long userId) {
        // 1. 查询酒店订单（业务逻辑）
        VipPaymentRecord order = membershipService.getRecordById(orderId);
        if (order == null || !userId.equals(order.getUserId())) {
            throw new ResourceNotFoundException("会员订单不存在");
        }

        // 2. 转换为统一支付参数（与业务无关）
        PayParamDTO param = new PayParamDTO();
        param.setOrderId(order.getOutTradeNo()); // 商户订单号（带业务前缀）
        param.setMoney(order.getTotalAmount()); // 支付金额
        param.setSubject("会员订阅"); // 订单标题
        param.setBody("超级会员订阅");
        return param;
    }

    /**
     * 支付成功后：置会员支付记录为成功，并把用户标记为 VIP。
     *
     * @param orderId   业务订单 ID（商户订单号）
     * @param payRecord 支付记录
     */
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
