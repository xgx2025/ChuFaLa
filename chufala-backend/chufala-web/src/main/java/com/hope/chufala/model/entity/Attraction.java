package com.hope.chufala.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class Attraction {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private String name;
    private String city;
    private String country;
    private String address;
    private String openTime;
    private Double adultTicketPrice;
    private Double childTicketPrice;
    private Double longitude;
    private Double latitude;
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
    @TableField(exist = false)
    private Double distance;   // 与用户的距离（公里）
}
