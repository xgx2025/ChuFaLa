package com.hope.chufala.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hope.chufala.model.entity.PayRecord;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface PayRecordMapper extends BaseMapper<PayRecord> {
    PayRecord selectByOrderId(Long orderId);

    List<PayRecord> selectByOrderIdForUpdate(Long orderId);
}
