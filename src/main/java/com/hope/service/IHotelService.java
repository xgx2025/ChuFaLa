package com.hope.service;

import com.hope.domain.dto.HotelFormDTO;
import com.hope.domain.dto.HotelPageQueryDTO;
import com.hope.domain.entity.Hotel;
import com.hope.domain.entity.HotelReview;
import com.hope.domain.vo.PageResult;

public interface IHotelService {
    /**
     * 添加酒店
     * @param hotel
     * @return
     */
    boolean addHotel(Hotel hotel);

    // 普通分页：查询全部酒店
    PageResult<Hotel> queryAllHotels(Integer page, Integer size);

    // 普通分页：按评分排名查询酒店
    PageResult<Hotel> queryHotelsByScoreRank(HotelPageQueryDTO  query);

    // 游标分页：按评分排名查询（适合大页码）
    PageResult<Hotel> queryHotelsByScoreRankCursor(
            Double lastAvgScore,
            Long lastHotelId,
            Integer size
    );

    // 提交评论（自动更新酒店评分）
    void submitReview(HotelReview review);
}
