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
    /** 每页大小 */
    private Integer size;
    /** 下一页游标；第一页不传 */
    private String cursor;
    /** 景点星级筛选 */
    private Integer stars;
    /** 预留排序参数；当前列表固定按评分降序、ID 降序查询 */
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
