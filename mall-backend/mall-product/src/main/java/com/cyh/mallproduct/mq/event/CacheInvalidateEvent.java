package com.cyh.mallproduct.mq.event;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * 缓存失效事件消息体
 * <p>
 * 业务数据变更后发送到 MQ，由消费者异步清除 Redis 缓存。
 * 支持精确 key 删除 + SCAN 模式删除（替代 KEYS 命令）。
 */
@Data
@Accessors(chain = true)
public class CacheInvalidateEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 缓存域（用于日志追踪） */
    private CacheDomain domain;

    /** 精确 key 列表，直接 DEL */
    private List<String> exactKeys;

    /** SCAN 模式列表，用于清除列表/分页类缓存（非阻塞，替代 KEYS） */
    private List<String> scanPatterns;
}