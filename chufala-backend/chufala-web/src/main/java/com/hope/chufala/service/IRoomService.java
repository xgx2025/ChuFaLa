package com.hope.chufala.service;

import java.time.LocalDate;
import java.util.Map;

/**
 * 房型服务。
 *
 * @author 谢光湘
 */
public interface IRoomService {

    /**
     * 查询房型价格。
     *
     * <p>注意：当前实现为占位桩，恒返回 0.0；真实价格请使用
     * {@code RoomMapper.selectRoomPrice}。
     *
     * @param roomTypeId 房型 ID
     * @return 价格（元/晚）
     */
    Double selectRoomPrice(Long roomTypeId);

    /**
     * 计算房型总价并生成价格签名。
     *
     * @param roomTypeId 房型 ID
     * @param checkIn    入住日期
     * @param checkOut   离店日期
     * @param roomCount  房间数
     * @return 含订单原始数据与签名的键值对
     */
    Map<String, String> calculateRoomTotalPrice(Long roomTypeId, LocalDate checkIn, LocalDate checkOut,int roomCount);

    /**
     * 查询指定入住区间内可售的最少房量（按每晚取最短板）。
     *
     * @param roomTypeId 房型 ID
     * @param checkIn    入住日期
     * @param checkOut   离店日期
     * @return 可售房量
     */
    int getAvailableStock(Long roomTypeId, LocalDate checkIn, LocalDate checkOut);
}
