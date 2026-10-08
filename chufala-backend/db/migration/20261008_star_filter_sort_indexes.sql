-- 酒店和景点列表允许只按星级、或同时按城市与星级筛选。
-- 原有评分/价格排序索引缺少 stars 前缀；数据量增大后需扫描大量不匹配的行，
-- 或走星级索引后再 filesort。以下索引对应这两种筛选范围及实际列表排序。
-- 先执行 20261008_cursor_pagination_indexes.sql；hotel 表需存在旧索引 idx_star_rating。
-- 在生产大表上执行前评估索引构建时间、磁盘空间和写入开销。

-- 新索引以 stars 为左前缀，可替代仅含 stars 的旧索引。
ALTER TABLE hotel
    DROP INDEX idx_star_rating,
    ADD INDEX idx_hotel_stars_rating_id (stars, overall_rating DESC, id ASC),
    ADD INDEX idx_hotel_city_stars_rating_id (city, stars, overall_rating DESC, id ASC),
    ADD INDEX idx_hotel_stars_price_id (stars, price ASC, id ASC),
    ADD INDEX idx_hotel_city_stars_price_id (city, stars, price ASC, id ASC);

-- 景点列表固定按评分降序、ID 降序排序。
ALTER TABLE attraction
    ADD INDEX idx_attraction_stars_rating_id (stars, rating DESC, id DESC),
    ADD INDEX idx_attraction_city_stars_rating_id (city, stars, rating DESC, id DESC);
