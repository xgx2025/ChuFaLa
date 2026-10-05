package com.hope.chufala.service.impl;

import com.hope.chufala.common.util.SignaturePriceUtils;
import com.hope.chufala.mapper.RoomMapper;
import com.hope.chufala.mapper.RoomDailyStockMapper;
import com.hope.chufala.service.IRoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Map;

/**
 * 房型服务实现。
 *
 * <p>总价计算前先校验库存（按入住区间取每晚最短板），再由 SignaturePriceUtils
 * 生成带签名的订单原始数据，供下单时校验价格未被篡改。
 *
 * @author 谢光湘
 */
@Service
public class RoomServiceImpl implements IRoomService {

    @Autowired
    private RoomMapper roomMapper;
    @Autowired
    private RoomDailyStockMapper roomDailyStockMapper;
    @Autowired
    private SignaturePriceUtils signaturePriceUtils;

    /**
     * 查询房型价格。
     *
     * <p>占位实现：恒返回 0.0，真实价格取自 RoomMapper.selectRoomPrice。
     *
     * @param roomTypeId 房型 ID
     * @return 固定 0.0
     */
    @Override
    public Double selectRoomPrice(Long roomTypeId) {
        return 0.0;
    }

    /**
     * 计算房型总价并生成价格签名。
     *
     * <p>房间数或库存不满足时抛 IllegalArgumentException；价格为 null 视为房型不存在。
     *
     * @param roomTypeId 房型 ID
     * @param checkIn    入住日期
     * @param checkOut   离店日期
     * @param roomCount  房间数
     * @return 含订单原始数据（data）与签名（signature）的键值对
     */
    @Override
    public Map<String, String> calculateRoomTotalPrice(Long roomTypeId, LocalDate checkIn, LocalDate checkOut,int roomCount) {
        if (roomCount <= 0) {
            throw new IllegalArgumentException("房间数量无效");
        }
        if (getAvailableStock(roomTypeId, checkIn, checkOut) < roomCount) {
            throw new IllegalArgumentException("所选日期的房间库存不足");
        }
        Double price = roomMapper.selectRoomPrice(roomTypeId);
        if (price == null) {
            throw new IllegalArgumentException("房型不存在");
        }
        long nightNum = ChronoUnit.DAYS.between(checkIn, checkOut);
        double totalPrice = price * nightNum * roomCount;
        return signaturePriceUtils.generatePriceWithSignature(roomTypeId.toString(), checkIn.toString(), checkOut.toString(),nightNum,roomCount, totalPrice);
    }

    /**
     * 查询指定入住区间内可售的最少房量（按每晚取最短板）。
     *
     * @param roomTypeId 房型 ID
     * @param checkIn    入住日期
     * @param checkOut   离店日期
     * @return 可售房量
     */
    @Override
    public int getAvailableStock(Long roomTypeId, LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null || checkOut == null || !checkOut.isAfter(checkIn)) {
            throw new IllegalArgumentException("入住日期无效");
        }
        if (ChronoUnit.DAYS.between(checkIn, checkOut) > 365) {
            throw new IllegalArgumentException("最多预订 365 晚");
        }
        Integer available = roomDailyStockMapper.selectMinAvailableStock(roomTypeId, checkIn, checkOut);
        if (available == null) {
            throw new IllegalArgumentException("房型不存在");
        }
        return available;
    }
}
