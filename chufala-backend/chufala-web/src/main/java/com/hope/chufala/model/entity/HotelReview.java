package com.hope.chufala.model.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class HotelReview {
    private Long id;
    private Long hotelId;
    private Long userId;
    private int rating;
    private String content;
    private LocalDateTime reviewTime;
}
