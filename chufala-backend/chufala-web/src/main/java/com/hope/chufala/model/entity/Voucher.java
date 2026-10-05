package com.hope.chufala.model.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 优惠券实体，对应 voucher 表。
 *
 * <p>当前仅映射了主键，业务字段尚未落地。
 *
 * @author 谢光湘
 */
@Data
@NoArgsConstructor
public class Voucher {
    /** 主键；序列化为字符串避免前端精度丢失 */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
}
