package com.hope.chufala.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 酒店订单实体，对应 hotel_order 表。
 *
 * <p>状态取值见 {@code HotelOrderStatus}："待支付" / "已支付" / "已取消"，
 * 状态流转与 room_daily_stock 的库存扣减/回补严格绑定。
 *
 * @author 谢光湘
 */
@Data
@NoArgsConstructor
public class HotelOrder {
    /** 自增主键 */
    private Long id;
    /** 业务订单号，关联同一次下单产生的多条记录；序列化为字符串 */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long orderId;
    /** 下单用户 ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;
    /** 酒店 ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long hotelId;
    /** 房型 ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long roomTypeId;
    /** 房型名称 */
    private String roomType;
    /** 预订房间数 */
    private Integer roomCount;
    /** 入住晚数 */
    private Integer nightNum;
    /** 订单总额（元） */
    private Double totalPrice;
    /** 实付金额（元），为扣除优惠后的金额 */
    private Double actualPrice;
    /** 订单状态，取值 "待支付" / "已支付" / "已取消" */
    private String orderStatus;
    /** 入住状态；命名沿用了数据库下划线风格，当前代码中未见其他引用 */
    private String check_in_status;
    /** 支付方式 */
    private String paymentMethod;
    /** 优惠金额（元） */
    private Double discount_amount;
    /** 入住人姓名 */
    private String guestName;
    /** 入住人手机号 */
    private String guestPhone;
    /** 入住人邮箱 */
    private String guestEmail;
    /** 特殊要求 */
    private String specialRequest;
    /** 入住日期 */
    private LocalDate checkIn;
    /** 离店日期 */
    private LocalDate checkOut;
    /** 预计到店时间 */
    private String arrivalTime;
    /** 下单时间 */
    private LocalDateTime bookTime;
    /** 支付时间 */
    private LocalDateTime paidTime;
    /** 订单标题 */
    private String title;

    /** 酒店名称，非持久化字段，列表查询时联表填充 */
    @TableField(exist = false)
    private String hotelName;
    /** 酒店地址，非持久化字段，列表查询时联表填充 */
    @TableField(exist = false)
    private String address;


}
