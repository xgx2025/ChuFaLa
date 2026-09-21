package com.hope.chufala.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class Room {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private String name;
    private Double  price;
    private int area;
    private String facilities;
    private String bed;
    private String people;
    private String floor;
    private Integer status;
    @TableField(exist = false)
    private List<String> imageList;
    private Integer stock;
    @JsonIgnore
    private String createTime;
}
