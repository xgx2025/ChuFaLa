package com.hope.chufala.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import com.hope.chufala.model.vo.PointVO;
import com.hope.chufala.model.dto.PayParamDTO;
import com.hope.chufala.infra.RedisData;

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
