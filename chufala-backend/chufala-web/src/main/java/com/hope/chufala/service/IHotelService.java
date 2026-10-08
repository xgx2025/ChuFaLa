package com.hope.chufala.service;

import com.hope.chufala.model.dto.HotelPageQueryDTO;
import com.hope.chufala.model.entity.Hotel;
import com.hope.chufala.model.entity.HotelReview;
import com.hope.chufala.model.entity.Room;
import com.hope.chufala.model.vo.HotelInfoVO;
import com.hope.chufala.common.model.vo.PageResult;

import java.util.List;

/**
 * 酒店服务。
 *
 * <p>提供酒店游标列表、详情查询与评价提交。
 *
 * @author 谢光湘
 */
public interface IHotelService {
    /**
     * 添加酒店
     *
     * @param hotel 酒店实体
     * @return 是否成功
     */
    boolean addHotel(Hotel hotel);

    /**
     * 按条件和排序方式游标分页查询酒店；评分和价格排序的前 3 页优先读取 Redis。
     *
     * @param query 筛选条件、排序方式、页大小、用户坐标及可选游标
     * @return 分页结果；nextCursor 仅有下一页时返回
     */
    PageResult<Hotel> queryHotelsByScoreRank(HotelPageQueryDTO  query);

    /**
     * 查询酒店详情（含图片与房型列表）。
     *
     * @param id 酒店 ID
     * @return 酒店详情
     */
    Hotel getHotelDetail(Long id);

    /**
     * 查询房型信息。
     *
     * @param id 房型 ID
     * @return 房型
     */
    Room getRoomInfo(Long id);

    /**
     * 按城市查询酒店信息。
     *
     * @param city 城市名称
     * @return 酒店信息列表
     */
    List<HotelInfoVO> findHotelByCity(String city);

//    List<HotelInfoVO> findHotelByCity(String city);

    // 提交评论（自动更新酒店评分）
    /**
     * 提交评价，并同步更新酒店的评分与评论数冗余字段。
     *
     * @param review 评价内容
     */
    void submitReview(HotelReview review);

    /**
     * 按城市查询酒店简单信息。
     *
     * @param city 城市名称
     * @return 酒店信息列表
     */
    List<HotelInfoVO> findHotelSimpleByCity(String city);

    /**
     * 按 ID 查询酒店简单信息。
     *
     * @param id 酒店 ID
     * @return 酒店信息
     */
    HotelInfoVO findHotelSimpleById(Long id);
}
