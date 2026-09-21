package com.hope.chufala.service.impl;

import com.hope.chufala.common.util.SignaturePriceUtils;
import com.hope.chufala.mapper.RoomMapper;
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
    private SignaturePriceUtils signaturePriceUtils;

    @Override
    public Double selectRoomPrice(Long roomTypeId) {
        return 0.0;
    }

    @Override
    public Map<String, String> calculateRoomTotalPrice(Long roomTypeId, LocalDate checkIn, LocalDate checkOut,int roomCount) {
        Double price = roomMapper.selectRoomPrice(roomTypeId);
        long nightNum = ChronoUnit.DAYS.between(checkIn, checkOut);
        double totalPrice = price * nightNum * roomCount;
        return signaturePriceUtils.generatePriceWithSignature(roomTypeId.toString(), checkIn.toString(), checkOut.toString(),nightNum,roomCount, totalPrice);
    }
}
