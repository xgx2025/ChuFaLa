-- ============================================================
-- 20261022_pay_record_status_enum.sql
--
-- 修复：pay_record.status 枚举缺少退款态，导致「已取消订单收到支付成功回调」
--       这条路径上的整笔回调事务回滚。
--
-- 证据链：
--   AlipayServiceImpl.applyPaidTrade() 捕获 OrderAlreadyCancelledException 后，
--   执行 UPDATE pay_record SET status='REFUND_REQUIRED'（第 241 行），
--   而列定义为 enum('WAIT_PAY','SUCCESS','FAIL')。
--   本机 sql_mode 含 STRICT_TRANS_TABLES，实测报：
--       ERROR 1265 (01000): Data truncated for column 'status' at row 1
--
--   后果：DataIntegrityViolationException 未被 catch → 冒泡出 handleNotify
--         → 事务回滚 → 返回 HTTP 500（而非约定的 "fail" 字符串）
--         → 支付宝按 25 小时退避策略持续重试，每次都在同一行失败
--         → 用户已付款、订单仍为已取消、pay_record 仍为 WAIT_PAY、无任何退款线索
--
-- 影响面：仅「已取消订单收到支付成功回调」一条路径。正常支付（WAIT_PAY→SUCCESS）
--         不受影响，这也是它长期未被发现的原因。PaymentNotifyRegressionTest 用
--         mock 的 Mapper 断言参数值，不真正写库，因此测试通过不代表可用。
--
-- 执行前置检查：
--   SELECT @@sql_mode;                                        -- 确认含 STRICT_TRANS_TABLES
--   SELECT status, COUNT(*) FROM pay_record GROUP BY status;   -- 本机实测 39 WAIT_PAY / 37 SUCCESS
--
-- 锁与耗时：MySQL 8.0 中向 ENUM 列表末尾追加值属于 INPLACE 变更
--   （ALGORITHM=INPLACE, LOCK=NONE），不重建表、不阻塞并发读写。
--   只有把新值插在中间、改变顺序或删值才会退化为 COPY（重建表 + 阻塞写入）。
-- ============================================================

ALTER TABLE `pay_record`
  MODIFY COLUMN `status` ENUM('WAIT_PAY', 'SUCCESS', 'FAIL', 'REFUND_REQUIRED', 'REFUNDED')
    CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci
    NOT NULL DEFAULT 'WAIT_PAY'
    COMMENT '支付状态：WAIT_PAY-待支付，SUCCESS-支付成功，FAIL-失败，REFUND_REQUIRED-需退款，REFUNDED-已退款';

-- 验证 1：列定义已包含两个退款态
SELECT COLUMN_NAME, COLUMN_TYPE
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'pay_record' AND COLUMN_NAME = 'status';

-- 验证 2：写入不再被截断（用临时表，不污染业务数据）
CREATE TEMPORARY TABLE `t_status_check` LIKE `pay_record`;
INSERT INTO `t_status_check` (`user_id`, `biz_type`, `order_id`, `status`, `money`)
VALUES (0, 'hotel', 0, 'REFUND_REQUIRED', 0.01);
INSERT INTO `t_status_check` (`user_id`, `biz_type`, `order_id`, `status`, `money`)
VALUES (0, 'hotel', 0, 'REFUNDED', 0.01);
SELECT `status`, COUNT(*) AS cnt FROM `t_status_check` GROUP BY `status`;
DROP TEMPORARY TABLE `t_status_check`;

-- 回滚（仅当没有任何行使用 REFUND_REQUIRED / REFUNDED 时才安全；
--       否则这些行会被截断为空串或直接报错，先确认：
--       SELECT COUNT(*) FROM pay_record WHERE status IN ('REFUND_REQUIRED','REFUNDED');）
-- ALTER TABLE `pay_record`
--   MODIFY COLUMN `status` ENUM('WAIT_PAY', 'SUCCESS', 'FAIL')
--     NOT NULL DEFAULT 'WAIT_PAY' COMMENT '支付状态';
