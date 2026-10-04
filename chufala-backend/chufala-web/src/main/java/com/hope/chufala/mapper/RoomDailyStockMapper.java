package com.hope.chufala.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;

@Mapper
public interface RoomDailyStockMapper {
    int initializeStock(@Param("roomTypeId") Long roomTypeId, @Param("stayDate") LocalDate stayDate);

    int decreaseStock(@Param("roomTypeId") Long roomTypeId, @Param("stayDate") LocalDate stayDate,
                      @Param("roomCount") int roomCount);

    int increaseStock(@Param("roomTypeId") Long roomTypeId, @Param("stayDate") LocalDate stayDate,
                      @Param("roomCount") int roomCount);

    Integer selectAvailableStock(@Param("roomTypeId") Long roomTypeId, @Param("stayDate") LocalDate stayDate);
}
