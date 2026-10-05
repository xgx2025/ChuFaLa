package com.hope.chufala.model.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 酒店简要信息 VO。
 *
 * <p>由 HotelMapper 的 XML 查询直接映射（resultType），用于 AI 规划的酒店推荐
 * 与按城市查询的轻量返回，仅包含列表展示所需字段。
 *
 * @author 谢光湘
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HotelInfoVO implements Serializable {
    private static final long serialVersionUID = 1L;
    /** 酒店 ID；序列化为字符串避免前端精度丢失 */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    /** 酒店名称 */
    private String name;
    /** 价格（元/晚） */
    private Double price;
    /** 图片 URL */
    private String image;
    /** 评分 */
    private String rating;
    /** 距市中心距离描述 */
    private String downtownDistance;
}
