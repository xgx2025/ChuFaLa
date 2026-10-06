-- MySQL 8：一个商户订单号只对应一条支付记录。
-- 如已有重复 order_id，唯一索引创建会失败；执行前先核对并处理重复流水。
-- SELECT order_id, COUNT(*) FROM pay_record GROUP BY order_id HAVING COUNT(*) > 1;
ALTER TABLE pay_record ADD UNIQUE INDEX uk_pay_record_order_id (order_id);
