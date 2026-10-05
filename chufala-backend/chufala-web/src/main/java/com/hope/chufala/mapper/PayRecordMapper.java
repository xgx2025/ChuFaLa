package com.hope.chufala.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hope.chufala.model.entity.PayRecord;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 支付流水 Mapper。
 *
 * <p>selectByOrderIdForUpdate 用于回调处理时对同一订单的支付记录加悲观锁，
 * 避免并发回调分别选中不同记录。
 *
 * @author 谢光湘
 */
@Mapper
public interface PayRecordMapper extends BaseMapper<PayRecord> {
    /**
     * 按业务订单 ID 查询支付记录。
     *
     * @param orderId 业务订单 ID
     * @return 支付记录
     */
    PayRecord selectByOrderId(Long orderId);

    /**
     * 按业务订单 ID 加锁查询支付记录（FOR UPDATE）。
     *
     * @param orderId 业务订单 ID
     * @return 支付记录列表
     */
    List<PayRecord> selectByOrderIdForUpdate(Long orderId);
}
