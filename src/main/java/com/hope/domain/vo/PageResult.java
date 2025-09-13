package com.hope.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult <T>{
    private List<T> data;
    private Long total;
    private Integer totalPage;
    private Integer page;
    private Integer size;
    private Long lastHotelId;   // 最后一条数据的ID（游标分页用）
    private Double lastAvgScore;// 最后一条数据的评分（游标分页用）
    private Boolean hasMore;    // 是否有更多数据

}
