-- 部署新版酒店支付代码前执行。旧流水默认 false，避免旧支付表单无绝对时限时被误取消。
ALTER TABLE pay_record
    ADD COLUMN absolute_expiry_enabled TINYINT(1) NOT NULL DEFAULT 0;
