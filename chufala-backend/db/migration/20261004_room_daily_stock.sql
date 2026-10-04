-- MySQL 8。停用旧版下单/取消入口后执行；此脚本可重复执行，但首次执行前请核对 room.stock
-- 当前是全局剩余数，且历史未取消订单均曾各扣减一次 room.stock。
CREATE TABLE IF NOT EXISTS room_daily_stock (
    room_type_id BIGINT NOT NULL,
    stay_date DATE NOT NULL,
    available_stock INT NOT NULL,
    PRIMARY KEY (room_type_id, stay_date),
    CONSTRAINT chk_room_daily_stock_nonnegative CHECK (available_stock >= 0)
);

CREATE TABLE IF NOT EXISTS room_stock_migration_audit (
    room_type_id BIGINT NOT NULL PRIMARY KEY,
    old_global_stock INT NOT NULL,
    base_capacity INT NOT NULL,
    migrated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

START TRANSACTION;

UPDATE hotel_order SET order_status = '待支付' WHERE order_status = '未支付';

-- 旧逻辑每个未取消订单只从 room.stock 扣过一次，与入住天数无关。
-- 因此用剩余数 + 全部未取消订单房间数，反推房型的基准总房量。
-- 已有逐日库存的房型属于新逻辑，不再反推。
INSERT IGNORE INTO room_stock_migration_audit (room_type_id, old_global_stock, base_capacity)
SELECT r.id, r.stock, r.stock + COALESCE(SUM(o.room_count), 0)
FROM room r
LEFT JOIN hotel_order o ON o.room_type_id = r.id
    AND o.order_status IN ('待支付', '未支付', '已支付')
WHERE NOT EXISTS (
    SELECT 1 FROM room_daily_stock s WHERE s.room_type_id = r.id
)
GROUP BY r.id, r.stock;

-- 只有尚处于迁移前库存值的房型才会更新，重跑不会再次累计订单数。
UPDATE room r
SET stock = (
    SELECT a.base_capacity FROM room_stock_migration_audit a WHERE a.room_type_id = r.id
)
WHERE EXISTS (
    SELECT 1 FROM room_stock_migration_audit a
    WHERE a.room_type_id = r.id AND r.stock = a.old_global_stock
);

COMMIT;

-- 核对结果；首次迁移后应无返回行。
SELECT r.id, r.stock, a.base_capacity
FROM room r JOIN room_stock_migration_audit a ON a.room_type_id = r.id
WHERE r.stock <> a.base_capacity;
