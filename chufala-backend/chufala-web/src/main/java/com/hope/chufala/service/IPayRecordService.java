package com.hope.chufala.service;

import com.hope.chufala.model.entity.PayRecord;

/**
 * 支付流水服务。
 *
 * @author 谢光湘
 */
public interface IPayRecordService {
    /**
     * 更新支付流水状态。
     *
     * @param payRecord 含目标状态与定位信息的流水记录
     * @return 是否更新成功
     */
    boolean updateStatus(PayRecord payRecord);
}
