package com.hope.service;

import java.time.LocalDate;
import java.util.Map;

public interface IRoomService {

    Double selectRoomPrice(Long roomTypeId);
    Map<String, String> calculateRoomTotalPrice(Long roomTypeId, LocalDate checkIn, LocalDate checkOut,int roomCount);
}
