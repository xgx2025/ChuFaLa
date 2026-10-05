package com.hope.chufala.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hope.chufala.model.entity.HotelOrder;
import org.apache.ibatis.annotations.Mapper;

/**
 * 酒店订单 Mapper。
 *
 * <p>仅使用 MyBatis-Plus 通用 CRUD 与 QueryWrapper / UpdateWrapper 条件构造，
 * 无自定义 XML SQL。
 *
 * @author 谢光湘
 */
@Mapper
public interface HotelOrderMapper extends BaseMapper<HotelOrder> {

}
