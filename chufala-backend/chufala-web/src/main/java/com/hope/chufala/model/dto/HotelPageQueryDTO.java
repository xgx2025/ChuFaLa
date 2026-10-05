package com.hope.chufala.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 酒店分页查询参数。
 *
 * <p>由 HotelController 以 {@code @ModelAttribute} 方式绑定（GET 查询串）。
 *
 * @author 谢光湘
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HotelPageQueryDTO {
    /** 页码，从 1 开始 */
    private Integer page;
    /** 每页大小 */
    private Integer size;
    /** 星级筛选 */
    private Integer stars;
    /** 城市筛选 */
    private String city;
    /** 价格上限（元） */
    private Double maxPrice;
    /** 价格下限（元） */
    private Double minPrice;
    /** 设施筛选列表，多选时按 AND 语义匹配 */
    private List<String> facilities;
    /** 用户纬度（仅用于距离计算，不进入缓存 Key） */
    private Double userLat;
    /** 用户经度（仅用于距离计算，不进入缓存 Key） */
    private Double userLng;
    /**
     * 排序方式。取值：recommended（默认，按评分）/ price-asc / price-desc / rating / distance。
     * 由 SQL 的 ORDER BY 承担，不再是前端对「已加载的几页」做客户端排序。
     */
    private String sort;
}
