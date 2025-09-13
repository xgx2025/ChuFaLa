package com.hope.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("attraction")
public class Attraction {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private String name;
    private String city;
    private String country;
    private String address;
    @TableField(value = "open_time")
    private String openTime;
    @TableField(value = "adult_ticket_price")
    private Double adultTicketPrice;
    @TableField(value = "child_ticket_price")
    private Double childTicketPrice;
    private String longitude;
    private String latitude;
    private String phone;
    private String image;
    private String description;
    @TableField(exist = false)
    private String location;
}
