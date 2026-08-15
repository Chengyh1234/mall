-- release_stock.lua
-- 释放冻结库存（超时/取消时调用）
-- KEYS[1] = sku:frozen:{skuId}
-- ARGV[1] = quantity（释放数量）
-- 返回: 1=成功

redis.call('DECRBY', KEYS[1], ARGV[1])
return 1