package com.hope.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
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
    @TableField(exist = false)
    private String mainImage;
    @TableField(exist = false)
    private List<String> otherImages;
    private String description;
    private Integer stars;
    private Double rating;
    @TableField(exist = false)
    private String location;
    private String tags;
    @TableField(exist = false)
    private int reviewCount;
}
