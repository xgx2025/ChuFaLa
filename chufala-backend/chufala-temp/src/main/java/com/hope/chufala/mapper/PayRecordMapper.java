package com.hope.chufala.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hope.chufala.domain.entity.PayRecord;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PayRecordMapper extends BaseMapper<PayRecord> {
    PayRecord selectByOrderId(Long orderId);
}
