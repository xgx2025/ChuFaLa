package com.hope.domain.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HotelInfo{
    private String name;
    private Double price;
    private String image;
    private String rating;
    private String downtownDistance;

}
