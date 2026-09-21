package com.hope.chufala.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Year;
import java.util.List;
import com.hope.chufala.model.vo.PointVO;
import com.hope.chufala.model.dto.PayParamDTO;
import com.hope.chufala.infra.RedisData;

@Data
@NoArgsConstructor
public class Hotel {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private String name;
    private Integer type;
    private String city;
    private String address;
    private Double price;
    private Double originalPrice;
    private String phone;
    @TableField(exist = false)
    private String mainImage;
    @TableField(exist = false)
    private List< String> otherImages;
    private String description;
    private String facilities;
    private Double longitude;
    private Double latitude;
    private Integer stars;
    private Double overallRating;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long reviewCount;
    private Year openYear;
    @TableField(exist = false)
    private String location;
    @TableField(exist = false)
    private List<Room> roomList;
    @TableField(exist = false)
    private Double distance; // 与用户的距离（公里）


}
