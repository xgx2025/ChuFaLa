package com.hope.chufala.model.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttractionInfo implements Serializable{

    @Serial
    private static final long serialVersionUID = 1L;
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
    private Double[] position;
    private String distance;
    private String drivingTime;


    public void setOtherInfo(AttractionInfo attractionInfo){
        this.name = attractionInfo.getName();
        this.address = attractionInfo.getAddress();
        this.position = attractionInfo.getPosition();
        this.price = attractionInfo.getPrice();
        this.rating = attractionInfo.getRating();
        this.description = attractionInfo.getDescription();
        this.tags = attractionInfo.getTags();
        this.image = attractionInfo.getImage();
        this.openTime = attractionInfo.getOpenTime();

    }
}
