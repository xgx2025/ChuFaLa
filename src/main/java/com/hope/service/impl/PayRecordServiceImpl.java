package com.hope.service.impl;

import com.hope.domain.entity.PayRecord;
import com.hope.mapper.PayRecordMapper;
import com.hope.service.IPayRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PayRecordServiceImpl implements IPayRecordService {

    @Autowired
    private PayRecordMapper payRecordMapper;

    @Override
    public boolean updateStatus(PayRecord payRecord) {
        return payRecordMapper.updateById(payRecord)>0;
    }
}
