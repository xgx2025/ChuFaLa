package com.hope.chufala.common.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 通用分页结果。
 *
 * <p>作为 {@code Result.data} 的载荷返回给前端；hasMore 用于前端无限滚动判断，
 * lastHotelId / lastAvgScore 为游标分页预留（当前主要使用 page + size 的偏移分页）。
 *
 * @author 谢光湘
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult <T>{
    /** 当前页数据 */
    private List<T> data;
    /** 总条数 */
    private Long total;
    /** 总页数 */
    private Integer totalPage;
    /** 当前页码 */
    private Integer page;
    /** 每页大小 */
    private Integer size;
    /** 最后一条数据的ID（游标分页用） */
    private Long lastHotelId;   // 最后一条数据的ID（游标分页用）
    /** 最后一条数据的评分（游标分页用） */
    private Double lastAvgScore;// 最后一条数据的评分（游标分页用）
    /** 是否还有更多数据 */
    private Boolean hasMore;    // 是否有更多数据

}
