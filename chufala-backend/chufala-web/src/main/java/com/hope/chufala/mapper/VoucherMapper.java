package com.hope.chufala.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hope.chufala.model.entity.Voucher;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券 Mapper。
 *
 * <p>仅使用 MyBatis-Plus 通用 CRUD；对应实体当前只映射了主键，业务字段尚未落地。
 *
 * @author 谢光湘
 */
@Mapper
public interface VoucherMapper extends BaseMapper<Voucher> {


}
