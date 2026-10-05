package com.hope.chufala.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 景点实体，对应 attraction 表。
 *
 * <p>既作为数据库映射对象，也作为景点列表/详情接口的返回体；
 * 带 {@code @TableField(exist = false)} 的字段不落库，由关联查询或坐标计算临时填充。
 *
 * @author 谢光湘
 */
@Data
@NoArgsConstructor
public class Attraction {
    /** 主键；序列化为字符串避免前端精度丢失 */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    /** 景点名称 */
    private String name;
    /** 所在城市 */
    private String city;
    /** 所在国家/地区 */
    private String country;
    /** 详细地址 */
    private String address;
    /** 开放时间描述 */
    private String openTime;
    /** 成人票价（元） */
    private Double adultTicketPrice;
    /** 儿童票价（元） */
    private Double childTicketPrice;
    /** 经度（BD09 坐标系） */
    private Double longitude;
    /** 纬度（BD09 坐标系） */
    private Double latitude;
    /** 联系电话 */
    private String phone;
    /** 主图 URL，非持久化字段 */
    @TableField(exist = false)
    private String mainImage;
    /** 其余图片 URL 列表，非持久化字段 */
    @TableField(exist = false)
    private List<String> otherImages;
    /** 景点描述 */
    private String description;
    /** 星级 */
    private Integer stars;
    /** 评分 */
    private Double rating;
    /** 位置描述文本，非持久化字段 */
    @TableField(exist = false)
    private String location;
    /** 标签（存储为分隔字符串） */
    private String tags;
    /** 评论数，非持久化字段 */
    @TableField(exist = false)
    private int reviewCount;
    /** 与用户的距离（公里），非持久化字段 */
    @TableField(exist = false)
    private Double distance;   // 与用户的距离（公里）
}
