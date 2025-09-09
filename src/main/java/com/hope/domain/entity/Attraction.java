package com.hope.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("attraction")
public class Attraction {
    private Long id;
    private String name;
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
