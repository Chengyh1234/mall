-- =====================================================
-- 退款相关新增字段（orders 表）
-- 使用说明：在现有 orders 表上执行以下 ALTER 语句
-- =====================================================

-- 1. 退款操作人ID（审核通过或拒接退款的操作用户ID）
ALTER TABLE orders
    ADD COLUMN refund_operator_id BIGINT DEFAULT NULL COMMENT '退款操作人ID' AFTER refund_from_status;

-- 2. 拒绝原因（拒接退款时填写）
ALTER TABLE orders
    ADD COLUMN reject_reason VARCHAR(500) DEFAULT NULL COMMENT '拒绝原因' AFTER refund_operator_id;

-- 3. 拒绝时间（拒接退款时记录）
ALTER TABLE orders
    ADD COLUMN rejected_at DATETIME DEFAULT NULL COMMENT '拒绝时间' AFTER reject_reason;