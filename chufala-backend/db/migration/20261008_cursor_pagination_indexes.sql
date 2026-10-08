-- 游标分页的排序索引。MySQL 8.0；执行前确认目标库尚无同名索引。
-- hotel 的 price 为 NOT NULL；DESC 价格排序可反向扫描 ASC(price, id) 索引。
ALTER TABLE hotel
    ADD INDEX idx_hotel_rating_id (overall_rating DESC, id ASC),
    ADD INDEX idx_hotel_city_rating_id (city, overall_rating DESC, id ASC),
    ADD INDEX idx_hotel_price_id (price ASC, id ASC),
    ADD INDEX idx_hotel_city_price_id (city, price ASC, id ASC);

ALTER TABLE attraction
    ADD INDEX idx_attraction_rating_id (rating DESC, id DESC),
    ADD INDEX idx_attraction_city_rating_id (city, rating DESC, id DESC);

ALTER TABLE hotel_order
    ADD INDEX idx_hotel_order_user_book (user_id, is_deleted, book_time DESC, id DESC);

-- 列表主图的标量子查询按外键和排序号查一张图。
ALTER TABLE hotel_image
    ADD INDEX idx_hotel_image_hotel_sort (hotel_id, sort_order);

ALTER TABLE attraction_image
    ADD INDEX idx_attraction_image_attraction_sort (attraction_id, sort_order);
