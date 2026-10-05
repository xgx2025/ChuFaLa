package com.hope.chufala.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;

/**
 * 逐日房量库存 Mapper（room_daily_stock 表）。
 *
 * <p>库存按「房型 × 日期」一行存储，扣减/回补都靠条件更新保证不超卖：
 * initializeStock 幂等建行（INSERT IGNORE，并按历史订单反推当日占用），
 * decreaseStock 要求剩余量足够（影响行数为 0 即表示库存不足），
 * increaseStock 用于取消订单时回补。读操作另有实时聚合兜底。
 *
 * @author 谢光湘
 */
@Mapper
public interface RoomDailyStockMapper {
    /**
     * 幂等初始化某房型某日的库存行。
     *
     * @param roomTypeId 房型 ID
     * @param stayDate   住宿日期
     * @return 影响行数（已存在时为 0）
     */
    int initializeStock(@Param("roomTypeId") Long roomTypeId, @Param("stayDate") LocalDate stayDate);

    /**
     * 扣减某房型某日库存（条件更新，库存不足时不生效）。
     *
     * @param roomTypeId 房型 ID
     * @param stayDate   住宿日期
     * @param roomCount  扣减数量
     * @return 影响行数，1 表示扣减成功
     */
    int decreaseStock(@Param("roomTypeId") Long roomTypeId, @Param("stayDate") LocalDate stayDate,
                      @Param("roomCount") int roomCount);

    /**
     * 回补某房型某日库存。
     *
     * @param roomTypeId 房型 ID
     * @param stayDate   住宿日期
     * @param roomCount  回补数量
     * @return 影响行数
     */
    int increaseStock(@Param("roomTypeId") Long roomTypeId, @Param("stayDate") LocalDate stayDate,
                      @Param("roomCount") int roomCount);

    /**
     * 查询某房型某日的可售房量（带实时聚合兜底）。
     *
     * @param roomTypeId 房型 ID
     * @param stayDate   住宿日期
     * @return 可售房量，房型不存在时为 null
     */
    Integer selectAvailableStock(@Param("roomTypeId") Long roomTypeId, @Param("stayDate") LocalDate stayDate);

    /**
     * 查询入住区间内每晚的最小可售房量（连住取最短板）。
     *
     * @param roomTypeId 房型 ID
     * @param checkIn    入住日期
     * @param checkOut   离店日期
     * @return 最小可售房量
     */
    Integer selectMinAvailableStock(@Param("roomTypeId") Long roomTypeId,
                                    @Param("checkIn") LocalDate checkIn,
                                    @Param("checkOut") LocalDate checkOut);
}
