package com.hope.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HotelReview {
    private Long id;
    private Long hotelId;
    private Long userId;
    private int rating;
    private String content;
    private LocalDateTime reviewTime;
}
