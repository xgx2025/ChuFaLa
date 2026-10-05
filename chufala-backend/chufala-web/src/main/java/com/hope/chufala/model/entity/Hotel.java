package com.hope.chufala.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Year;
import java.util.List;

/**
 * 酒店实体，对应 hotel 表（主键列名为 id，评论数列名为 review_count）。
 *
 * <p>既作为数据库映射对象，也直接充当酒店列表/详情接口的返回体；
 * 带 {@code @TableField(exist = false)} 的字段不落库，由关联查询或坐标计算临时填充。
 *
 * @author 谢光湘
 */
@Data
@NoArgsConstructor
public class Hotel {
    /** 主键；序列化为字符串避免前端精度丢失 */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    /** 酒店名称 */
    private String name;
    /** 酒店类型标识 */
    private Integer type;
    /** 所在城市 */
    private String city;
    /** 详细地址 */
    private String address;
    /** 当前售价（元/晚） */
    private Double price;
    /** 原价（元/晚），用于展示划线价 */
    private Double originalPrice;
    /** 联系电话 */
    private String phone;
    /** 主图 URL，非持久化字段，由 hotel_image 关联查询填充 */
    @TableField(exist = false)
    private String mainImage;
    /** 其余图片 URL 列表，非持久化字段 */
    @TableField(exist = false)
    private List< String> otherImages;
    /** 酒店描述 */
    private String description;
    /** 设施列表（存储为分隔字符串；多选筛选按 AND 语义匹配） */
    private String facilities;
    /** 经度（BD09 坐标系） */
    private Double longitude;
    /** 纬度（BD09 坐标系） */
    private Double latitude;
    /** 星级 */
    private Integer stars;
    /** 综合评分 */
    private Double overallRating;
    /** 评论数，对应列 review_count；序列化为字符串 */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long reviewCount;
    /** 开业年份 */
    private Year openYear;
    /** 位置描述文本，非持久化字段 */
    @TableField(exist = false)
    private String location;
    /** 房型列表，非持久化字段，详情接口填充 */
    @TableField(exist = false)
    private List<Room> roomList;
    /** 与用户的距离（公里），非持久化字段，按坐标实时计算 */
    @TableField(exist = false)
    private Double distance; // 与用户的距离（公里）


}
