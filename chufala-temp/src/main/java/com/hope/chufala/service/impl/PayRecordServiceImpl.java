package com.hope.chufala.service.impl;

import com.hope.chufala.domain.entity.PayRecord;
import com.hope.chufala.mapper.PayRecordMapper;
import com.hope.chufala.service.IPayRecordService;
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
