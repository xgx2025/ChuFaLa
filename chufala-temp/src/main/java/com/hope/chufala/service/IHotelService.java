package com.hope.chufala.service;

import com.hope.chufala.domain.dto.HotelPageQueryDTO;
import com.hope.chufala.domain.entity.Hotel;
import com.hope.chufala.domain.entity.HotelReview;
import com.hope.chufala.domain.entity.Room;
import com.hope.chufala.domain.vo.HotelInfo;
import com.hope.chufala.domain.vo.PageResult;

import java.util.List;

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

    Hotel getHotelDetail(Long id);

    Room getRoomInfo(Long id);

    List<HotelInfo> findHotelByCity(String city);

//    List<HotelInfo> findHotelByCity(String city);

    // 提交评论（自动更新酒店评分）
    void submitReview(HotelReview review);

    List<HotelInfo> findHotelSimpleByCity(String city);

    HotelInfo findHotelSimpleById(Long id);
}
