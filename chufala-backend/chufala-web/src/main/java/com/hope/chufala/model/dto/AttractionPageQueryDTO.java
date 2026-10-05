package com.hope.chufala.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 景点分页查询参数。
 *
 * <p>由 AttractionController 以 {@code @ModelAttribute} 方式绑定（GET 查询串）。
 *
 * @author 谢光湘
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttractionPageQueryDTO {
    /** 搜索关键词 */
    private String keyword;
    /** 查询偏移量，对应 SQL LIMIT 的 offset */
    private Integer offset;
    /** 每页大小 */
    private Integer size;
    /** 景点星级筛选 */
    private Integer stars;
    /** 排序方式 */
    private String sortBy;
    /** 城市名称 */
    private String city;
    /** 标签筛选列表 */
    private List<String> tags;
    /** 用户纬度 */
    private Double userLat;
    /** 用户经度 */
    private Double userLng;

}
