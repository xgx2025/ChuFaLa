package com.hope.chufala.service.impl;

import cn.hutool.core.lang.Snowflake;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hope.chufala.common.util.ThreadLocalUtils;
import com.hope.chufala.model.entity.VipPaymentRecord;
import com.hope.chufala.mapper.VipPaymentRecordMapper;
import com.hope.chufala.service.IMembershipService;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
@Service
public class MembershipServiceImpl implements IMembershipService {
    @Autowired
    private VipPaymentRecordMapper vipPaymentRecordMapper;
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

    @Override
    public VipPaymentRecord getRecordById(Long orderId) {
        QueryWrapper<VipPaymentRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("out_trade_no",orderId);
        return vipPaymentRecordMapper.selectOne(queryWrapper);
    }
}
