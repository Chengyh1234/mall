-- confirm_stock.lua
-- 确认扣除库存（支付成功时调用）
-- KEYS[1] = sku:stock:{skuId}
-- KEYS[2] = sku:frozen:{skuId}
-- ARGV[1] = quantity（确认数量）
-- 返回: 1=成功

redis.call('DECRBY', KEYS[1], ARGV[1])
redis.call('DECRBY', KEYS[2], ARGV[1])
return 1