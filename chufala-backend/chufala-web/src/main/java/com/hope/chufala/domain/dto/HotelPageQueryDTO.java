package com.hope.chufala.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HotelPageQueryDTO {
    private Integer page;
    private Integer size;
    private Integer stars;
    private String city;
    private Double maxPrice;
    private Double minPrice;
    private List<String> facilities;
    private Double userLat;
    private Double userLng;
    /**
     * 排序方式。取值：recommended（默认，按评分）/ price-asc / price-desc / rating / distance。
     * 由 SQL 的 ORDER BY 承担，不再是前端对「已加载的几页」做客户端排序。
     */
    private String sort;
}
