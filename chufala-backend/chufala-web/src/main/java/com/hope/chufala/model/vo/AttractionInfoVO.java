package com.hope.chufala.model.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 景点信息 VO。
 *
 * <p>由 AttractionMapper 的 XML 查询直接映射（resultType），同时作为 AI 行程规划
 * 的候选景点载体。position 为 [经度, 纬度] 数组。
 *
 * @author 谢光湘
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttractionInfoVO implements Serializable{

    @Serial
    private static final long serialVersionUID = 1L;
    /** 景点 ID */
    private String id;
    /** 景点名称 */
    private String name;
    /** 详细地址 */
    private String address;
    /** 开放时间，对应列 open_time */
    @TableField(value = "open_time")
    private String openTime;
    /** 票价（元） */
    private Double price;
    /** 图片 URL */
    private String image;
    /** 标签 */
    private String tags;
    /** 评分 */
    private String rating;
    /** 景点描述 */
    private String description;
    /** 游玩提示 */
    private String tip;
    /** 坐标数组，格式为 [经度, 纬度] */
    private Double[] position;
    /** 与用户的距离描述 */
    private String distance;
    /** 驾车耗时描述 */
    private String drivingTime;


    /**
     * 用另一个景点 VO 的展示字段覆盖当前对象（不含 id、距离、驾车耗时等上下文信息）。
     *
     * @param attractionInfo 数据来源
     */
    public void setOtherInfo(AttractionInfoVO attractionInfo){
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
