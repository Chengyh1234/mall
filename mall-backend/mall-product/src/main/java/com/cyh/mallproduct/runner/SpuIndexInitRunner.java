package com.cyh.mallproduct.runner;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cyh.mallproduct.entity.Brand;
import com.cyh.mallproduct.entity.Category;
import com.cyh.mallproduct.entity.Spu;
import com.cyh.mallproduct.feign.StoreClient;
import com.cyh.mallproduct.es.entity.SpuIndex;
import com.cyh.mallproduct.es.repository.SpuIndexRepository;
import com.cyh.mallproduct.mapper.BrandMapper;
import com.cyh.mallproduct.mapper.CategoryMapper;
import com.cyh.mallproduct.mapper.SpuMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * SPU 索引初始化 Runner
 * <p>
 * 应用启动时，自动创建 ES 索引（若不存在）并全量同步所有上架商品到 ES。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SpuIndexInitRunner implements ApplicationRunner {

    private final SpuMapper spuMapper;
    private final CategoryMapper categoryMapper;
    private final BrandMapper brandMapper;
    private final StoreClient storeClient;
    private final SpuIndexRepository spuIndexRepository;

    @Override
    public void run(ApplicationArguments args) {
        try {
            // 1. 检查索引是否存在，不存在则创建
            if (!spuIndexRepository.existsIndex()) {
                boolean created = spuIndexRepository.createIndex();
                log.info("ES 索引 mall_spu 不存在，已创建: {}", created);
            } else {
                log.info("ES 索引 mall_spu 已存在，跳过创建");
            }

            // 2. 全量同步所有上架商品（status=1）且未删除
            log.info("开始全量同步 SPU 到 ES...");
            List<Spu> allSpu = spuMapper.selectList(new QueryWrapper<Spu>().
                    eq("status",1).
                    eq("is_deleted", false));
            if (allSpu == null || allSpu.isEmpty()) {
                log.info("数据库无 SPU 数据，跳过全量同步");
                return;
            }

            // 回填分类名和品牌名
            fillCategoryAndBrandNames(allSpu);

            List<SpuIndex> indexList = allSpu.stream()
                    .map(this::toSpuIndex)
                    .collect(Collectors.toList());

            spuIndexRepository.bulkSave(indexList);
            log.info("全量同步完成，共同步 {} 条 SPU 到 ES", indexList.size());

        } catch (Exception e) {
            log.error("SPU 索引初始化失败: {}", e.getMessage(), e);
        }
    }

    private void fillCategoryAndBrandNames(List<Spu> spuList) {
        // 简单回填，全量数据量不大时直接查
        for (Spu spu : spuList) {
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
                    com.cyh.mallcommon.utils.Result<com.cyh.mallcommon.dto.StoreInfoVo> result = storeClient.getStoreDetail(spu.getStoreId());
                    if (result != null && result.getData() != null) {
                        spu.setStoreName(result.getData().getName());
                    }
                } catch (Exception e) {
                    log.warn("Feign调用mall-store获取店铺信息失败, storeId: {}", spu.getStoreId(), e);
                }
            }
        }
    }

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