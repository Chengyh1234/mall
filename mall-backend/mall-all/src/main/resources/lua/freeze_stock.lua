-- freeze_stock.lua
-- 冻结库存（下单时调用）
-- KEYS[1] = sku:stock:{skuId}
-- KEYS[2] = sku:frozen:{skuId}
-- ARGV[1] = quantity（冻结数量）
-- 返回: 1=成功, 0=库存不足

local total = tonumber(redis.call('GET', KEYS[1]) or 0)
local frozen = tonumber(redis.call('GET', KEYS[2]) or 0)
local quantity = tonumber(ARGV[1])
local available = total - frozen

if available >= quantity then
    redis.call('INCRBY', KEYS[2], ARGV[1])
    return 1
else
    return 0
end