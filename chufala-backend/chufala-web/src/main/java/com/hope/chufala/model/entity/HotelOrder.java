package com.hope.chufala.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import com.hope.chufala.model.vo.PointVO;
import com.hope.chufala.model.dto.PayParamDTO;
import com.hope.chufala.infra.RedisData;

@Data
@NoArgsConstructor
public class HotelOrder {
    private Long id;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long orderId;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long hotelId;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long roomTypeId;
    private String roomType;
    private Integer roomCount;
    private Integer nightNum;
    private Double totalPrice;
    private Double actualPrice;
    private String orderStatus;
    private String check_in_status;
    private String paymentMethod;
    private Double discount_amount;
    private String guestName;
    private String guestPhone;
    private String guestEmail;
    private String specialRequest;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private String arrivalTime;
    private LocalDateTime bookTime;
    private LocalDateTime paidTime;
    private String title;

    @TableField(exist = false)
    private String hotelName;
    @TableField(exist = false)
    private String address;


}
