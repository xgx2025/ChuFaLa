package com.hope.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("hotel")
public class Hotel {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private String name;
    private Integer type;
    private String city;
    private String address;
    private Double price;
    @TableField(value = "original_price")
    private Double originalPrice;
    private String phone;
    @TableField(value = "cover_image")
    private String coverImage;
    private String description;
    private String facilities;
    private Double longitude;
    private Double latitude;
    @TableField(value = "star_rating")
    private Integer stars;
    private Double rating;
    @TableField(value = "comment_count")
    private Double overallRating;
    @TableField(value = "review_count")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long reviewCount;
    @TableField(exist = false)
    private String location;
    @TableField(exist = false)
    private Double distance; // 与用户的距离（公里）


}
