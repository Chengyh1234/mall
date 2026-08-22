package com.cyh.mallorder.mq.event;

import lombok.Data;
import lombok.experimental.Accessors;
import java.io.Serializable;
import java.util.List;

/**
 * 库存同步事件消息
 * <p>
 * 当订单创建或取消时，发送该消息将 Redis 中的库存变更
 * 同步回 MySQL 数据库，保证数据最终一致性。
 */
@Data
@Accessors(chain = true)
public class StockSyncEvent implements Serializable {
    private List<Long> skuIds;
}