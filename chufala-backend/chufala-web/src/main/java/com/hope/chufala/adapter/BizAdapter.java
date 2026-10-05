package com.hope.chufala.adapter;

import com.hope.chufala.model.dto.PayParamDTO;
import com.hope.chufala.model.entity.PayRecord;

/**
 * 业务适配器接口：定义「业务订单」与「支付核心」之间的交互规范。
 *
 * <p>目的是让支付流程只依赖统一抽象——新增业务类型时只需实现本接口，
 * 无需改动 AlipayServiceImpl。实现类的 Bean 名必须为 {@code {bizType小写}Adapter}。
 *
 * @author 谢光湘
 */
// 业务适配器接口：定义商品业务与支付核心的交互规范
public interface BizAdapter {
    // 构建支付参数（将业务订单转换为统一支付参数）
    /**
     * 构建统一支付参数（将业务订单转换为支付核心可用的结构）。
     *
     * @param bizId  业务订单 ID
     * @param userId 下单用户 ID
     * @return 统一支付参数
     */
    PayParamDTO buildPayParam(Long bizId, Long userId);
    // 处理支付成功（支付成功后触发的业务逻辑）
    /**
     * 处理支付成功后的业务逻辑（推进订单状态等）。
     *
     * @param bizId     业务订单 ID
     * @param payRecord 支付记录
     */
    void handlePaySuccess(Long bizId, PayRecord payRecord);
}
