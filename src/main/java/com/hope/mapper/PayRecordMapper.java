package com.hope.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hope.domain.entity.PayRecord;
import org.apache.ibatis.annotations.Mapper;

import java.io.Serializable;

@Mapper
public interface PayRecordMapper extends BaseMapper<PayRecord> {

    PayRecord selectByOrderId(Long orderId);
}
