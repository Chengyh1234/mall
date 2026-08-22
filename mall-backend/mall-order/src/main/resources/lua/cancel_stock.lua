-- KEYS[1] = sku:stock:{skuId}
-- ARGV[1] = quantity
-- 返回: 1=成功

redis.call('INCRBY', KEYS[1], ARGV[1])
return 1