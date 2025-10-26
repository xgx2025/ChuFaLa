package com.hope.domain.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttractionInfo {
    private String id;
    private String name;
    private String address;
    @TableField(value = "open_time")
    private String openTime;
    private Double price;
    private String image;
    private String tags;
    private String rating;
    private String description;
    private String tip;
    private double[] position;
    private String distance;
    private String drivingTime;
}
