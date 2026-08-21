package com.cyh.mallproduct.mq.event;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * SPU 同步事件消息体
 * <p>
 * SPU 增删改后发送到 MQ，由消费者异步同步 Elasticsearch 索引。
 * 三种类型：CREATE / UPDATE / DELETE
 */
@Data
@Accessors(chain = true)
public class SpuSyncEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    /** SPU ID */
    private Long spuId;

    /** 事件类型：CREATE / UPDATE / DELETE */
    private String type;
}