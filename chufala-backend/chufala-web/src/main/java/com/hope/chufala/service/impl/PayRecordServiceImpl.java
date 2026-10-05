package com.hope.chufala.service.impl;

import com.hope.chufala.model.entity.PayRecord;
import com.hope.chufala.mapper.PayRecordMapper;
import com.hope.chufala.service.IPayRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 支付流水服务实现。
 *
 * <p>按主键做局部更新，PayRecord 字段均为包装类型，null 字段不会被写回。
 *
 * @author 谢光湘
 */
@Service
public class PayRecordServiceImpl implements IPayRecordService {

    @Autowired
    private PayRecordMapper payRecordMapper;

    /**
     * 更新支付流水状态。
     *
     * @param payRecord 含目标状态与主键的流水记录
     * @return 是否更新成功
     */
    @Override
    public boolean updateStatus(PayRecord payRecord) {
        return payRecordMapper.updateById(payRecord)>0;
    }
}
