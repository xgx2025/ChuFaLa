package com.hope.chufala.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 房型实体，对应 room 表。
 *
 * <p>注意 {@code stock} 的语义：自逐日库存改造后，它是该房型的
 * 「基准总房量 base_capacity」，而非实时剩余房量；
 * 实时余量按日期存放在 room_daily_stock 表，需按入住区间查询。
 *
 * @author 谢光湘
 */
@Data
@NoArgsConstructor
public class Room {
    /** 主键；序列化为字符串避免前端精度丢失 */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    /** 房型名称 */
    private String name;
    /** 价格（元/晚） */
    private Double  price;
    /** 面积（平方米） */
    private int area;
    /** 设施列表（存储为分隔字符串） */
    private String facilities;
    /** 床型描述 */
    private String bed;
    /** 可住人数描述 */
    private String people;
    /** 楼层描述 */
    private String floor;
    /** 房型状态 */
    private Integer status;
    /** 房型图片 URL 列表，非持久化字段 */
    @TableField(exist = false)
    private List<String> imageList;
    /** 基准总房量 base_capacity，非实时余量；实时余量见 room_daily_stock 表 */
    private Integer stock;
    /** 创建时间，响应体不输出 */
    @JsonIgnore
    private String createTime;
}
