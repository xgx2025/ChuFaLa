package com.hope.chufala.model.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 酒店评价实体，对应 hotel_review 表。
 *
 * <p>评分与评论数的冗余字段落在 hotel 表（overall_rating / review_count），
 * 新增评价时需在同一事务内同步更新，见 HotelServiceImpl#submitReview。
 *
 * @author 谢光湘
 */
@Data
@NoArgsConstructor
public class HotelReview {
    /** 自增主键 */
    private Long id;
    /** 被评价酒店 ID */
    private Long hotelId;
    /** 评价用户 ID */
    private Long userId;
    /** 评分 */
    private int rating;
    /** 评价内容 */
    private String content;
    /** 评价时间 */
    private LocalDateTime reviewTime;
}
