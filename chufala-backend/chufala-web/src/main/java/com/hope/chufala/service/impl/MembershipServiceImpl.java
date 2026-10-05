package com.hope.chufala.service.impl;

import cn.hutool.core.lang.Snowflake;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hope.chufala.model.entity.VipPaymentRecord;
import com.hope.chufala.mapper.VipPaymentRecordMapper;
import com.hope.chufala.service.IMembershipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * 会员（VIP）服务实现。
 *
 * <p>会员价格当前硬编码为 19.9 元；订单号由 Snowflake 生成并作为商户订单号
 * （out_trade_no）对外使用。
 *
 * @author 谢光湘
 */
@Service
public class MembershipServiceImpl implements IMembershipService {
    @Autowired
    private VipPaymentRecordMapper vipPaymentRecordMapper;
    /**
     * 创建会员购买订单。
     *
     * @param userId 用户 ID
     * @return 商户订单号（字符串）
     */
    @Override
    public String createMembershipOrder(Long userId) {
        Snowflake snowflake = new Snowflake(1, 1);
        Long orderId = snowflake.nextId();
        VipPaymentRecord paymentRecord = new VipPaymentRecord();
        paymentRecord.setOutTradeNo(orderId);
        paymentRecord.setUserId(userId);
        paymentRecord.setTotalAmount(BigDecimal.valueOf(19.9)); // 会员费用
        vipPaymentRecordMapper.insert(paymentRecord);
        return String.valueOf(orderId);
    }

    /**
     * 按商户订单号查询会员支付记录。
     *
     * @param orderId 商户订单号
     * @return 支付记录
     */
    @Override
    public VipPaymentRecord getRecordById(Long orderId) {
        QueryWrapper<VipPaymentRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("out_trade_no",orderId);
        return vipPaymentRecordMapper.selectOne(queryWrapper);
    }
}
