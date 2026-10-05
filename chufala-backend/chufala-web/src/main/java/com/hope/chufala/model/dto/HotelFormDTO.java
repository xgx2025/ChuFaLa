package com.hope.chufala.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 酒店新增/编辑表单参数。
 *
 * <p>部分字段名与 Hotel 实体不同：coverImage 对应 mainImage，starRating 对应 stars。
 *
 * @author 谢光湘
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HotelFormDTO {
    /** 酒店名称 */
    private String name;
    /** 酒店类型标识 */
    private Integer type;
    /** 所在城市 */
    private String city;
    /** 详细地址 */
    private String address;
    /** 售价（元/晚） */
    private Double price;
    /** 联系电话 */
    private String phone;
    /** 封面图 URL，对应实体字段 mainImage */
    private String coverImage;
    /** 酒店描述 */
    private String description;
    /** 经度 */
    private Double longitude;
    /** 纬度 */
    private Double latitude;
    /** 星级，对应实体字段 stars */
    private Integer starRating;
}
