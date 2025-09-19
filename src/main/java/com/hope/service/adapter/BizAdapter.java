package com.hope.service.adapter;

import com.hope.domain.entity.PayParam;
import com.hope.domain.entity.PayRecord;

// 业务适配器接口：定义商品业务与支付核心的交互规范
public interface BizAdapter {
    // 构建支付参数（将业务订单转换为统一支付参数）
    PayParam buildPayParam(Long bizId);
    // 处理支付成功（支付成功后触发的业务逻辑）
    void handlePaySuccess(Long bizId, PayRecord payRecord);
}
