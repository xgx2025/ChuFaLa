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

@Service
public class RoomServiceImpl implements IRoomService {

    @Autowired
    private RoomMapper roomMapper;
    @Autowired
    private RoomDailyStockMapper roomDailyStockMapper;
    @Autowired
    private SignaturePriceUtils signaturePriceUtils;

    @Override
    public Double selectRoomPrice(Long roomTypeId) {
        return 0.0;
    }

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
