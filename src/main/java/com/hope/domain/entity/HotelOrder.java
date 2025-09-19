package com.hope.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HotelOrder {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long roomTypeId;
    @TableField(value = "room_type")
    private String roomType;
    private String title;
    private String guestName;
    private String guestPhone;
    private LocalDateTime checkIn;
    private LocalDateTime checkOut;
    private String arrivalTime;
    private Double price;
    private Double totalPrice;
    private Integer nightNum;
    private Integer count;
    @TableField(value = "payment_status")
    private String paymentStatus;

}
