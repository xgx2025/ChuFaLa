package com.hope.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HotelOrder {
    private Long id;
    @JsonSerialize(using = ToStringSerializer.class)
    @TableField(value = "order_id")
    private Long orderId;
    @TableField(value = "user_id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;
    @TableField(value = "hotel_id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long hotelId;
    @JsonSerialize(using = ToStringSerializer.class)
    @TableField(value = "room_type_id")
    private Long roomTypeId;
    @TableField(value = "room_type")
    private String roomType;
    @TableField(value = "room_count")
    private Integer roomCount;
    @TableField(value = "night_num")
    private Integer nightNum;
    @TableField(value = "total_price")
    private Double totalPrice;
    @TableField(value = "actual_price")
    private Double actualPrice;
    @TableField(value = "order_status")
    private String orderStatus;
    @TableField(value = "check_in_status")
    private String check_in_status;
    @TableField(value = "payment_method")
    private String paymentMethod;
    @TableField(value = "discount_amount")
    private Double discount_amount;
    @TableField(value = "guest_name")
    private String guestName;
    @TableField(value = "guest_phone")
    private String guestPhone;
    @TableField(value = "guest_email")
    private String guestEmail;
    @TableField(value = "special_request")
    private String specialRequest;
    @TableField(value = "check_in")
    private LocalDate checkIn;
    @TableField(value = "check_out")
    private LocalDate checkOut;
    @TableField(value = "arrival_time")
    private String arrivalTime;
    @TableField(value = "book_time")
    private LocalDateTime bookTime;
    @TableField(value = "paid_time")
    private LocalDateTime paidTime;
    @TableField(value = "title")
    private String title;

    @TableField(exist = false)
    private String hotelName;
    @TableField(exist = false)
    private String address;


}
