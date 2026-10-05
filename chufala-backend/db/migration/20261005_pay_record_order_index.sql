-- MySQL 8：支付回调使用 order_id 查找并锁定支付记录。
-- 本地旧数据存在同一订单的多条 WAIT_PAY 记录，因此这里建立非唯一索引，不删除历史记录。
-- 部署新版支付回调前执行一次；如果索引已经存在，请跳过本语句。
ALTER TABLE pay_record ADD INDEX idx_pay_record_order_id_id (order_id, id);
