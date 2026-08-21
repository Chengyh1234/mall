package com.cyh.mallproduct.mq.consumer;

import com.cyh.mallcommon.dto.StoreInfoVo;
import com.cyh.mallcommon.utils.Result;
import com.cyh.mallproduct.config.RabbitMQConfig;
import com.cyh.mallproduct.entity.Brand;
import com.cyh.mallproduct.entity.Category;
import com.cyh.mallproduct.entity.Spu;
import com.cyh.mallproduct.es.entity.SpuIndex;
import com.cyh.mallproduct.es.repository.SpuIndexRepository;
import com.cyh.mallproduct.mapper.BrandMapper;
import com.cyh.mallproduct.mapper.CategoryMapper;
import com.cyh.mallproduct.mapper.SpuMapper;
import com.cyh.mallproduct.feign.StoreClient;
import com.cyh.mallproduct.mq.event.SpuSyncEvent;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * SPU 同步消费者
 * <p>
 * 消费 SPU 增删改事件，异步同步 Elasticsearch 索引。
 * 统一入口：所有 SPU 变更（新增、更新、删除、上下架）均通过此消费者同步 ES。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SpuSyncConsumer {

    private final SpuMapper spuMapper;
    private final CategoryMapper categoryMapper;
    private final BrandMapper brandMapper;
    private final StoreClient storeClient;
    private final SpuIndexRepository spuIndexRepository;

    @RabbitListener(queues = RabbitMQConfig.SPU_SYNC_QUEUE)
    public void handleSpuSync(SpuSyncEvent event, Message message, Channel channel) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            Long spuId = event.getSpuId();
            String type = event.getType();
            log.info("消费 SPU 同步事件, spuId: {}, type: {}", spuId, type);

            switch (type) {
                case "CREATE":
                case "UPDATE":
                    // 从 DB 查询完整数据后写入 ES
                    Spu spu = spuMapper.selectById(spuId);
                    if (spu == null) {
                        log.warn("SPU 不存在，跳过同步: spuId={}", spuId);
                        break;
                    }
                    // 回填分类名、品牌名和店铺名
                    if (spu.getCategoryId() != null) {
                        Category category = categoryMapper.selectById(spu.getCategoryId());
                        if (category != null) spu.setCategoryName(category.getName());
                    }
                    if (spu.getBrandId() != null) {
                        Brand brand = brandMapper.selectById(spu.getBrandId());
                        if (brand != null) spu.setBrandName(brand.getName());
                    }
                    if (spu.getStoreId() != null) {
                        try {
                            Result<StoreInfoVo> storeResult = storeClient.getStoreDetail(spu.getStoreId());
                            if (storeResult != null && storeResult.getData() != null) {
                                spu.setStoreName(storeResult.getData().getName());
                            }
                        } catch (Exception e) {
                            log.warn("Feign调用mall-store获取店铺信息失败, storeId: {}", spu.getStoreId(), e);
                        }
                    }
                    spuIndexRepository.save(toSpuIndex(spu));
                    log.info("ES 索引同步成功: spuId={}, type={}", spuId, type);
                    break;
                case "DELETE":
                    spuIndexRepository.delete(spuId);
                    log.info("ES 索引删除成功: spuId={}", spuId);
                    break;
                default:
                    log.warn("未知的 SPU 同步事件类型: {}", type);
            }

            channel.basicAck(deliveryTag, false);

        } catch (Exception e) {
            log.error("消费 SPU 同步事件失败, spuId: {}, type: {}, 异常: {}",
                    event.getSpuId(), event.getType(), e.getMessage(), e);
            channel.basicNack(deliveryTag, false, false);
        }
    }

    /**
     * Spu → SpuIndex 转换
     */
    private SpuIndex toSpuIndex(Spu spu) {
        return new SpuIndex()
                .setId(spu.getId())
                .setName(spu.getName())
                .setDescription(spu.getDescription())
                .setKeywords(spu.getKeywords())
                .setCategoryId(spu.getCategoryId())
                .setCategoryName(spu.getCategoryName())
                .setBrandId(spu.getBrandId())
                .setBrandName(spu.getBrandName())
                .setStoreId(spu.getStoreId())
                .setStoreName(spu.getStoreName())
                .setSellerId(spu.getSellerId())
                .setMinPrice(spu.getMinPrice())
                .setSales(spu.getSales())
                .setMainImage(spu.getMainImage())
                .setCreatedAt(spu.getCreatedAt());
    }
}