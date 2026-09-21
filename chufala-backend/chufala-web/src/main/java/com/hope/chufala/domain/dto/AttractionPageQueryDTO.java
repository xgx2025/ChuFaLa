package com.hope.chufala.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttractionPageQueryDTO {
    /**
     * 搜索关键词
     */
    private String keyword;
    /**
     * 当前页码
     */
    private Integer offset;
    /**
     * 每页大小
     */
    private Integer size;
    /**
     * 景点星级
     */
    private Integer stars;
    /**
     * 排序方式
     */
    private String sortBy;
    /**
     * 城市名称
     * */
    private String city;
    /**
     * 标签列表
     * */
    private List<String> tags;
    /**
     * 用户经度
     * */
    private Double userLat;
    /**
     * 用户纬度
     * */
    private Double userLng;

}
