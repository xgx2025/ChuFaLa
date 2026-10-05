package com.hope.chufala.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hope.chufala.model.entity.Hotel;
import com.hope.chufala.model.vo.HotelInfoVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

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

    // 普通分页：查询全部酒店（按ID排序）
    /**
     * 分页查询全部酒店（按 ID 排序）。
     *
     * @param offset 偏移量
     * @param size   页大小
     * @return 酒店列表
     */
    List<Hotel> selectAllByPage(
            @Param("offset") Integer offset,
            @Param("size") Integer size
    );

    // 普通分页：查询按评分排序的酒店
    // userLat / userLng 仅在 sort=distance 时参与 ORDER BY（Haversine），其余情况不生效
    /**
     * 按条件分页查询酒店（支持评分 / 价格 / 距离排序）。
     *
     * @param offset     偏移量
     * @param size       页大小
     * @param stars      星级筛选
     * @param city       城市筛选
     * @param maxPrice   价格上限
     * @param minPrice   价格下限
     * @param facilities 设施筛选（多选按 AND 语义）
     * @param sort       排序方式
     * @param userLat    用户纬度（仅 distance 排序生效）
     * @param userLng    用户经度（仅 distance 排序生效）
     * @return 酒店列表
     */
    List<Hotel> selectByScoreRankPage(
            @Param("offset") Integer offset,
            @Param("size") Integer size,
            @Param("stars") Integer stars,
            @Param("city") String city,
            @Param("maxPrice") Double maxPrice,
            @Param("minPrice") Double minPrice,
            @Param("facilities")List<String> facilities,
            @Param("sort") String sort,
            @Param("userLat") Double userLat,
            @Param("userLng") Double userLng
    );

    // 游标分页：按评分排序（基于上一页最后一条数据）
//    List<Hotel> selectByScoreRankCursor(
//            @Param("lastAvgScore") Double lastAvgScore,
//            @Param("lastHotelId") Long lastHotelId,
//            @Param("size") Integer size
//    );

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

    // 查询全部酒店总数
    /**
     * 查询全部酒店总数。
     *
     * @return 总数
     */
    Long countTotal();
    // 查询符合条件的酒店总数
    /**
     * 按条件查询酒店总数（条件需与 selectByScoreRankPage 一致）。
     *
     * @param stars      星级筛选
     * @param city       城市筛选
     * @param maxPrice   价格上限
     * @param minPrice   价格下限
     * @param facilities 设施筛选
     * @return 总数
     */
    Long countTotalByCondition(Integer stars, String city, Double maxPrice, Double minPrice, List<String> facilities);

    /**
     * 查询酒店图片 URL 列表。
     *
     * @param hotelId 酒店 ID
     * @return 图片 URL 列表
     */
    List<String> findHotelImage(Long hotelId);

    /**
     * 查询酒店名称与地址。
     *
     * @param hotelId 酒店 ID
     * @return 含 name / address 的 Map
     */
    Map<String,String>  findHotelNameAndAddress(Long hotelId);

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
