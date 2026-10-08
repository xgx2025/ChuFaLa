package com.hope.chufala.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hope.chufala.model.entity.Hotel;
import com.hope.chufala.model.vo.HotelInfoVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 酒店 Mapper。
 *
 * <p>自定义 SQL 由 HotelMapper.xml 提供。列表查询的排序由 {@code sort} 参数驱动
 * （评分 / 价格 / 距离），其中 distance 排序用 Haversine 公式，仅此时 userLat/userLng 生效。
 * 注意 hotel 表的评论数列名是 review_count、主键列名是 id。
 *
 * @author 谢光湘
 */
@Mapper
public interface HotelMapper extends BaseMapper<Hotel> {

    /**
     * 按排序值和 ID 游标查询酒店，最多返回 size 条。
     *
     * <p>评分、价格、距离排序由 sort 决定；lastId 为空时查询首页。距离排序使用
     * Haversine 原始值，userLat/userLng 只在此排序下参与 SQL。
     *
     * @param size       查询条数；服务层传入请求页大小加一，用于判断是否有下一页
     * @param stars      星级筛选
     * @param city       城市筛选
     * @param maxPrice   价格上限
     * @param minPrice   价格下限
     * @param facilities 设施筛选（多选按 AND 语义）
     * @param sort       排序方式
     * @param userLat    用户纬度（仅 distance 排序生效）
     * @param userLng    用户经度（仅 distance 排序生效）
     * @param lastId     上一页最后一条酒店的 ID；首页为 null
     * @param lastValue  上一页最后一条酒店的排序原值；评分为空时可为 null
     * @return 按排序规则排列的酒店列表
     */
    List<Hotel> selectByScoreRankPage(
            @Param("size") Integer size,
            @Param("stars") Integer stars,
            @Param("city") String city,
            @Param("maxPrice") Double maxPrice,
            @Param("minPrice") Double minPrice,
            @Param("facilities")List<String> facilities,
            @Param("sort") String sort,
            @Param("userLat") Double userLat,
            @Param("userLng") Double userLng,
            @Param("lastId") Long lastId,
            @Param("lastValue") Double lastValue
    );

    // 更新酒店评分和评论数
    /**
     * 更新酒店的综合评分与评论数冗余字段。
     *
     * @param hotelId         酒店 ID
     * @param newAvgScore     新评分
     * @param newCommentCount 新评论数
     */
    void updateScoreAndCount(
            @Param("hotelId") Long hotelId,
            @Param("newAvgScore") Double newAvgScore,
            @Param("newCommentCount") Integer newCommentCount
    );

    /**
     * 查询酒店图片 URL 列表。
     *
     * @param hotelId 酒店 ID
     * @return 图片 URL 列表
     */
    List<String> findHotelImage(Long hotelId);

    /**
     * 按城市查询酒店信息。
     *
     * @param city 城市名称
     * @return 酒店信息列表
     */
    List<HotelInfoVO> findHotelByCity(String city);

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
