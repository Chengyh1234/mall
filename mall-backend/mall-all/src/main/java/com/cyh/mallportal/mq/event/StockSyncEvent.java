package com.cyh.mallportal.mq.event;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * 库存同步事件消息体
 * <p>
 * Redis 库存变更后发送到 MQ，由消费者异步同步到 MySQL
 */
@Data
@Accessors(chain = true)
public class StockSyncEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 需要同步的 SKU ID 列表 */
    private List<Long> skuIds;
}