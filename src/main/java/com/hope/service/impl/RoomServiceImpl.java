package com.hope.service.impl;

import com.hope.mapper.RoomTypeMapper;
import com.hope.service.IRoomService;
import com.hope.utils.SignaturePriceUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;

@Service
public class RoomServiceImpl implements IRoomService {

    @Autowired
    private RoomTypeMapper roomTypeMapper;

    @Override
    public Double selectRoomPrice(Long roomTypeId) {
        return 0.0;
    }

    @Override
    public Map<String, String> calculateRoomTotalPrice(Long roomTypeId, LocalDate checkIn, LocalDate checkOut,int roomCount) {
        Double price = roomTypeMapper.selectRoomPrice(roomTypeId);
        long nightNum = ChronoUnit.DAYS.between(checkIn, checkOut);
        double totalPrice = price * nightNum * roomCount;
        return SignaturePriceUtil.generatePriceWithSignature(roomTypeId.toString(), checkIn.toString(), checkOut.toString(),nightNum,roomCount, totalPrice);
    }
}
