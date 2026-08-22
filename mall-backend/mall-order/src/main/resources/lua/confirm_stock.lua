-- KEYS[1] = sku:stock:{skuId}
-- KEYS[2] = sku:frozen:{skuId}
-- ARGV[1] = quantity
-- 返回: 1=成功

redis.call('DECRBY', KEYS[1], ARGV[1])
redis.call('DECRBY', KEYS[2], ARGV[1])
return 1