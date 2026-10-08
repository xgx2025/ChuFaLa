package com.hope.chufala.common.model.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 通用分页结果。
 *
 * <p>作为 {@code Result.data} 的载荷返回给前端；hasMore 用于前端无限滚动判断，
 * 列表使用游标翻页；total 只在第一页计算，后续页可为空。
 *
 * @author 谢光湘
 */
@Data
@NoArgsConstructor
public class PageResult <T>{
    /** 当前页数据 */
    private List<T> data;
    /** 符合筛选条件的总条数；仅首页返回，后续页为 null */
    private Long total;
    /** 请求的每页大小，上限为 50 */
    private Integer size;
    /** 是否还有下一页 */
    private Boolean hasMore;

    /** 下一页游标；到达末页时为空 */
    private String nextCursor;

}
