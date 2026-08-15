-- 已经支付的订单取消订单，redis库存扣除回退，也就是增加
-- KEYS[1] = sku:stock:{skuId}
-- ARGV[1] = quantity（确认数量）
-- 返回: 1=成功
redis.call('INCRBY', KEYS[1], ARGV[1])
return 1