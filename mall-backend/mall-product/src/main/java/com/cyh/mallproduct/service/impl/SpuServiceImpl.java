package com.cyh.mallproduct.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cyh.mallcommon.constant.RedisConstants;
import com.cyh.mallcommon.exception.BusinessException;
import com.cyh.mallcommon.dto.UserProfileVo;
import com.cyh.mallproduct.entity.*;
import com.cyh.mallproduct.feign.UserClient;
import com.cyh.mallproduct.mapper.*;
import com.cyh.mallproduct.mq.event.CacheDomain;
import com.cyh.mallproduct.mq.event.CacheInvalidateEvent;
import com.cyh.mallproduct.mq.publisher.CacheEventPublisher;
import com.cyh.mallproduct.mq.publisher.SpuEventPublisher;
import com.cyh.mallproduct.service.SpuCacheService;
import com.cyh.mallproduct.service.SpuService;
import com.cyh.mallproduct.vo.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * SPU 商品服务实现
 *
 * <p>核心职责：SPU 的增删改查、上下架、搜索、缓存管理。</p>
 *
 * <p>缓存策略说明：
 * <ul>
 *   <li>分页列表（如 {@link #getPage}、{@link #searchByKeyword}）直接查 DB，不缓存。
 *       因为组合参数爆炸，缓存命中率极低，MySQL 索引覆盖下分页性能已足够。</li>
 *   <li>单个 SPU 详情（如 {@link #getSpuDetailById}）采用 Cache-Aside 模式，
 *       三种视角（公开/商家/管理）独立缓存。增删改操作精确清除对应 SPU 的缓存。</li>
 * </ul>
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SpuServiceImpl implements SpuService {

    private final SpuMapper spuMapper;

    private final UserClient userClient;

    /** SPU 详情缓存服务 */
    private final SpuCacheService spuCacheService;

    private final CacheEventPublisher cacheEventPublisher;

    private final SpuEventPublisher spuEventPublisher;

    private final CategoryMapper categoryMapper;

    private final BrandMapper brandMapper;

    private final SpuBasicAttrValueMapper spuBasicAttrValueMapper;

    private final SpuSaleAttrChoiceMapper spuSaleAttrChoiceMapper;

    private final SkuMapper skuMapper;

    /** 商家 SPU 数量上限 */
    private static final long MAX_SPU_PER_SELLER = 5000;

    // ==================== 增删改 ====================

    /**
     * 新增 SPU
     * <p>新增时无缓存可清除，仅在 SKU 操作更新最低售价时才触发缓存失效。</p>
     */

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long add(Spu spu) {
        // 校验商家 SPU 数量上限
        if (spu.getSellerId() != null) {
            Long currentCount = spuMapper.selectCount(
                    new LambdaQueryWrapper<Spu>()
                            .eq(Spu::getSellerId, spu.getSellerId())
            );
            if (currentCount >= MAX_SPU_PER_SELLER) {
                throw new BusinessException("商品数量已达上限（" + MAX_SPU_PER_SELLER + "个），请删除部分商品后再添加");
            }
        }
        // 校验同商家下商品名称唯一性
        if (spu.getName() != null && spu.getSellerId() != null) {
            Long count = spuMapper.selectCount(
                    new LambdaQueryWrapper<Spu>()
                            .eq(Spu::getSellerId, spu.getSellerId())
                            .eq(Spu::getName, spu.getName())
            );
            if (count > 0) {
                throw new BusinessException("该商家下已存在同名商品：" + spu.getName());
            }
        }
        // 初始化默认字段
        spu.setCreatedAt(LocalDateTime.now());
        spu.setUpdatedAt(LocalDateTime.now());
        spu.setStatus(0);                     // 新增默认下架
        if (spu.getSales() == null) {
            spu.setSales(0);                  // 默认销量 0
        }
        int result = spuMapper.insert(spu);
        if (result > 0) {
            spuEventPublisher.publishSync(spu.getId(), "CREATE");
            log.info("新增商品后发布 ES 同步事件: spuId={}", spu.getId());
        }
        return result > 0 ? spu.getId() : null;
    }

    /**
     * 逻辑删除 SPU
     * <p>删除后精确清除该 SPU 的详情缓存。</p>
     */
    @Override
    public boolean delete(Long id) {
        int result = spuMapper.deleteById(id);
        if (result > 0) {
            cacheEventPublisher.publishInvalidate(new CacheInvalidateEvent()
                    .setDomain(CacheDomain.SPU)
                    .setExactKeys(List.of(
                            RedisConstants.SPU_DETAIL_PREFIX + id,
                            RedisConstants.SPU_DETAIL_SELLER_PREFIX + id,
                            RedisConstants.SPU_DETAIL_ADMIN_PREFIX + id
                    )));
            spuEventPublisher.publishSync(id, "DELETE");
            log.info("删除商品后发布缓存失效事件和 ES 同步事件: spuId={}", id);
        }
        return result > 0;
    }

    /**
     * 恢复被逻辑删除的 SPU（设置 is_deleted=0）
     * <p>使用 UpdateWrapper 绕过 MyBatis-Plus {@code @TableLogic} 的自动过滤。</p>
     */
    @Override
    public boolean restore(Long id) {
        UpdateWrapper<Spu> wrapper = new UpdateWrapper<>();
        wrapper.eq("id", id);
        wrapper.set("is_deleted", 0);
        int result = spuMapper.update(null, wrapper);
        if (result > 0) {
            cacheEventPublisher.publishInvalidate(new CacheInvalidateEvent()
                    .setDomain(CacheDomain.SPU)
                    .setExactKeys(List.of(
                            RedisConstants.SPU_DETAIL_PREFIX + id,
                            RedisConstants.SPU_DETAIL_SELLER_PREFIX + id,
                            RedisConstants.SPU_DETAIL_ADMIN_PREFIX + id
                    )));
            spuEventPublisher.publishSync(id, "UPDATE");
            log.info("恢复商品后发布缓存失效事件和 ES 同步事件: spuId={}", id);
        }
        return result > 0;
    }

    /**
     * 更新 SPU 信息
     *
     * <p>变更分类时会级联清理旧分类下的属性绑定和 SKU，并强制下架，
     * 商家需在新分类下重新配置属性和 SKU 后才能再次上架。</p>
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean update(Spu spu) {
        // 1. 校验商品是否存在
        Spu oldSpu = spuMapper.selectById(spu.getId());
        if (oldSpu == null) {
            throw new BusinessException("商品不存在");
        }

        // 2. 校验同商家下商品名称唯一性（排除自身）
        if (spu.getName() != null) {
            Long sellerId = spu.getSellerId() != null ? spu.getSellerId() : oldSpu.getSellerId();
            if (sellerId != null) {
                Long count = spuMapper.selectCount(
                        new LambdaQueryWrapper<Spu>()
                                .eq(Spu::getSellerId, sellerId)
                                .eq(Spu::getName, spu.getName())
                                .ne(Spu::getId, spu.getId())
                );
                if (count > 0) {
                    throw new BusinessException("该商家下已存在同名商品：" + spu.getName());
                }
            }
        }

        // 3. 分类变更 → 清理旧分类相关数据
        if (spu.getCategoryId() != null && !spu.getCategoryId().equals(oldSpu.getCategoryId())) {
            // 3.1 校验新分类必须为叶子分类
            Long childCount = categoryMapper.selectCount(
                    new LambdaQueryWrapper<Category>()
                            .eq(Category::getParentId, spu.getCategoryId())
                            .eq(Category::getIsDeleted, false)
            );
            if (childCount > 0) {
                throw new BusinessException("该分类下有子分类，请选择叶子分类");
            }

            // 3.2 清理旧分类下的基本属性、销售属性和 SKU
            Long spuId = spu.getId();
            spuBasicAttrValueMapper.delete(
                    new LambdaQueryWrapper<SpuBasicAttrValue>()
                            .eq(SpuBasicAttrValue::getSpuId, spuId)
            );
            spuSaleAttrChoiceMapper.delete(
                    new LambdaQueryWrapper<SpuSaleAttrChoice>()
                            .eq(SpuSaleAttrChoice::getSpuId, spuId)
            );
            skuMapper.delete(
                    new LambdaQueryWrapper<Sku>()
                            .eq(Sku::getSpuId, spuId)
            );

            log.info("分类变更，已清理SPU {} 的旧属性绑定和SKU", spuId);
            spu.setStatus(0); // 强制下架
        }

        // 4. 更新并清除缓存
        spu.setUpdatedAt(LocalDateTime.now());
        int result = spuMapper.updateById(spu);
        if (result > 0) {
            cacheEventPublisher.publishInvalidate(new CacheInvalidateEvent()
                    .setDomain(CacheDomain.SPU)
                    .setExactKeys(List.of(
                            RedisConstants.SPU_DETAIL_PREFIX + spu.getId(),
                            RedisConstants.SPU_DETAIL_SELLER_PREFIX + spu.getId(),
                            RedisConstants.SPU_DETAIL_ADMIN_PREFIX + spu.getId()
                    )));
            spuEventPublisher.publishSync(spu.getId(), "UPDATE");
            log.info("更新商品后发布缓存失效事件和 ES 同步事件: spuId={}", spu.getId());
        }
        return result > 0;
    }

    // ==================== 基础查询 ====================

    /**
     * 根据 ID 查询 SPU（不缓存，供内部使用）
     */
    @Override
    public Spu getById(Long id) {
        return spuMapper.selectById(id);
    }

    // ==================== 分页列表（直接 DB，不缓存） ====================

    /**
     * 公开分页查询 SPU 列表
     * <p>分页参数组合多，缓存命中率低，直接走 DB 查询。</p>
     */
    @Override
    public List<SpuVo> getPage(Spu spu, Integer page, Integer pageSize) {
        int pageNum = page != null ? page : 1;
        int pageSizeNum = pageSize != null ? pageSize : 10;

        Page<Spu> pageParam = new Page<>(pageNum, pageSizeNum);
        LambdaQueryWrapper<Spu> queryWrapper = buildQueryWrapper(spu);
        queryWrapper.orderByDesc(Spu::getCreatedAt);
        IPage<Spu> result = spuMapper.selectPage(pageParam, queryWrapper);

        return result.getRecords().stream()
                .map(this::convertToSpuVO)
                .collect(Collectors.toList());
    }

    /**
     * 按分类 ID 列表分页查询（供分类导航使用）
     */
    @Override
    public List<Spu> getPageByCategoryIds(Spu spu, Integer page, Integer pageSize, List<Long> categoryIds) {
        Page<Spu> pageParam = new Page<>(page != null ? page : 1, pageSize != null ? pageSize : 10);
        LambdaQueryWrapper<Spu> queryWrapper = new LambdaQueryWrapper<>();
        if (categoryIds != null && !categoryIds.isEmpty()) {
            queryWrapper.in(Spu::getCategoryId, categoryIds);
        }
        if (spu != null && spu.getId() != null) {
            queryWrapper.eq(Spu::getId, spu.getId());
        }
        if (spu != null && spu.getBrandId() != null) {
            queryWrapper.eq(Spu::getBrandId, spu.getBrandId());
        }
        if (spu != null && StringUtils.hasText(spu.getName())) {
            queryWrapper.like(Spu::getName, spu.getName());
        }
        queryWrapper.orderByDesc(Spu::getCreatedAt);
        IPage<Spu> result = spuMapper.selectPage(pageParam, queryWrapper);
        return result.getRecords();
    }

    /**
     * 多字段模糊搜索（商品名、分类名、品牌名）
     * <p>走自定义 SQL JOIN 查询，不缓存。</p>
     */
    @Override
    public List<SpuVo> searchByKeyword(List<Long> categoryIds, String keyword, Long brandId, Integer page, Integer pageSize) {
        int pageNum = page != null && page > 0 ? page : 1;
        int pageSizeNum = pageSize != null ? pageSize : 10;
        int offset = (pageNum - 1) * pageSizeNum;

        List<Spu> records = spuMapper.searchByKeyword(categoryIds, keyword, brandId, offset, pageSizeNum);
        return records.stream()
                .map(this::convertToSpuVO)
                .collect(Collectors.toList());
    }

    /**
     * 统计搜索结果数量
     */
    @Override
    public int countSearchByKeyword(List<Long> categoryIds, String keyword, Long brandId) {
        return spuMapper.countSearchByKeyword(categoryIds, keyword, brandId);
    }

    // ==================== 商家端 ====================

    /**
     * 商家分页查看自己的商品列表
     */
    @Override
    public List<SpuSellerVo> getPageBySellerId(Long sellerId, Integer status, String keyword, Integer page, Integer pageSize) {
        Page<Spu> pageParam = new Page<>(page != null ? page : 1, pageSize != null ? pageSize : 10);
        LambdaQueryWrapper<Spu> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Spu::getSellerId, sellerId);
        if (status != null) {
            queryWrapper.eq(Spu::getStatus, status);
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            queryWrapper.like(Spu::getName, keyword.trim());
        }
        queryWrapper.orderByDesc(Spu::getCreatedAt);
        IPage<Spu> result = spuMapper.selectPage(pageParam, queryWrapper);
        List<Spu> spuList = result.getRecords();
        fillCategoryAndBrandNames(spuList);

        return spuList.stream()
                .map(this::convertToSpuSellerVO)
                .collect(Collectors.toList());
    }

    /**
     * 统计商家商品数量
     */
    @Override
    public int countBySellerId(Long sellerId, Integer status, String keyword) {
        LambdaQueryWrapper<Spu> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Spu::getSellerId, sellerId);
        if (status != null) {
            queryWrapper.eq(Spu::getStatus, status);
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            queryWrapper.like(Spu::getName, keyword.trim());
        }
        return spuMapper.selectCount(queryWrapper).intValue();
    }

    // ==================== 管理端 ====================

    /**
     * 管理员分页查看全平台商品
     */
    @Override
    public List<SpuAdminVo> getPageAll(Integer status, String keyword, Integer page, Integer pageSize) {
        Page<Spu> pageParam = new Page<>(page != null ? page : 1, pageSize != null ? pageSize : 10);
        LambdaQueryWrapper<Spu> queryWrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            queryWrapper.eq(Spu::getStatus, status);
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            queryWrapper.like(Spu::getName, keyword.trim());
        }
        queryWrapper.orderByDesc(Spu::getCreatedAt);
        IPage<Spu> result = spuMapper.selectPage(pageParam, queryWrapper);
        List<Spu> spuList = result.getRecords();
        fillCategoryAndBrandNames(spuList);

        return spuList.stream()
                .map(this::convertToSpuAdminVO)
                .collect(Collectors.toList());
    }

    /**
     * 统计全平台商品数量
     */
    @Override
    public int countAll(Integer status, String keyword) {
        LambdaQueryWrapper<Spu> queryWrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            queryWrapper.eq(Spu::getStatus, status);
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            queryWrapper.like(Spu::getName, keyword.trim());
        }
        return spuMapper.selectCount(queryWrapper).intValue();
    }

    /**
     * 校验商家对 SPU 的归属权
     */
    @Override
    public Spu getByIdAndSellerId(Long id, Long sellerId) {
        LambdaQueryWrapper<Spu> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Spu::getId, id);
        queryWrapper.eq(Spu::getSellerId, sellerId);
        return spuMapper.selectOne(queryWrapper);
    }

    // ==================== SKU 联动 ====================

    /**
     * 更新 SPU 的最低 SKU 售价
     *
     * <p>取该 SPU 下所有启用 SKU 的最低价写入 {@code spu.min_price} 字段，
     * 并清除详情缓存。此方法在 SKU 增/删/改/启用/禁用时被调用。</p>
     */
    @Override
    public void updateMinPriceForSpu(Long spuId) {
        LambdaQueryWrapper<Sku> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Sku::getSpuId, spuId);
        wrapper.eq(Sku::getStatus, 1);
        wrapper.orderByAsc(Sku::getPrice);
        wrapper.last("LIMIT 1");
        Sku sku = skuMapper.selectOne(wrapper);

        BigDecimal minPrice = sku != null ? sku.getPrice() : BigDecimal.ZERO;

        Spu spu = new Spu();
        spu.setId(spuId);
        spu.setMinPrice(minPrice);
        spuMapper.updateById(spu);

        // 最低售价变化 → 详情页、列表页都需要更新 → 发布缓存失效事件 + ES 同步事件
        cacheEventPublisher.publishInvalidate(new CacheInvalidateEvent()
                .setDomain(CacheDomain.SPU)
                .setExactKeys(List.of(
                        RedisConstants.SPU_DETAIL_PREFIX + spuId,
                        RedisConstants.SPU_DETAIL_SELLER_PREFIX + spuId,
                        RedisConstants.SPU_DETAIL_ADMIN_PREFIX + spuId
                )));
        spuEventPublisher.publishSync(spuId, "UPDATE");
        log.info("更新SPU[{}]的最低售价: {}, 已发布缓存失效事件和 ES 同步事件", spuId, minPrice);
    }

    /**
     * 检查 SPU 下是否存在启用状态的 SKU
     * <p>用于上架前置校验：不允许上架一个无启用 SKU 的 SPU。</p>
     */
    @Override
    public boolean hasEnabledSku(Long spuId) {
        LambdaQueryWrapper<Sku> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Sku::getSpuId, spuId);
        wrapper.eq(Sku::getStatus, 1);
        wrapper.last("LIMIT 1");
        return skuMapper.selectOne(wrapper) != null;
    }

    // ==================== 详情查询（带缓存） ====================

    /**
     * 获取公开 SPU 详情（Cache-Aside）
     *
     * <p>先查缓存，未命中则查 DB 并回写，TTL 30 分钟。
     * 返回 {@link SpuDetailVo}，含商品信息 + 商家名称/头像。</p>
     */
    @Override
    public SpuDetailVo getSpuDetailById(Long id) {
        SpuDetailVo cached = spuCacheService.getSpuDetail(id);
        if (cached != null) {
            return cached;
        }

        Spu spu = spuMapper.selectById(id);
        if (spu == null) {
            return null;
        }
        fillCategoryAndBrandNames(Collections.singletonList(spu));

        SpuDetailVo vo = new SpuDetailVo();
        vo.setId(spu.getId());
        vo.setName(spu.getName());
        vo.setCategoryId(spu.getCategoryId());
        vo.setCategoryName(spu.getCategoryName());
        vo.setBrandId(spu.getBrandId());
        vo.setBrandName(spu.getBrandName());
        vo.setDescription(spu.getDescription());
        vo.setMainImage(spu.getMainImage());
        vo.setImages(spu.getImages());
        vo.setUnit(spu.getUnit());
        vo.setKeywords(spu.getKeywords());
        vo.setSales(spu.getSales());
        vo.setMinPrice(spu.getMinPrice());

        // 回填商家信息
        vo.setSellerId(spu.getSellerId());
        if (spu.getSellerId() != null) {
            UserProfileVo seller = userClient.getUserDetail(spu.getSellerId()).getData();
            if (seller != null) {
                vo.setSellerUsername(seller.getUsername());
                vo.setSellerAvatar(seller.getAvatar());
            }
        }

        spuCacheService.setSpuDetail(id, vo);
        return vo;
    }

    /**
     * 获取商家端 SPU 管理详情（Cache-Aside）
     *
     * <p>TTL 10 分钟。返回 {@link SpuSellerDetailVo}，含商品信息 +
     * 商家姓名/手机号等敏感信息。</p>
     */
    @Override
    public SpuSellerDetailVo getSpuSellerDetailById(Long id) {
        SpuSellerDetailVo cached = spuCacheService.getSpuSellerDetail(id);
        if (cached != null) {
            return cached;
        }

        Spu spu = spuMapper.selectById(id);
        if (spu == null) {
            return null;
        }
        fillCategoryAndBrandNames(Collections.singletonList(spu));

        SpuSellerDetailVo vo = new SpuSellerDetailVo();
        vo.setId(spu.getId());
        vo.setName(spu.getName());
        vo.setCategoryId(spu.getCategoryId());
        vo.setCategoryName(spu.getCategoryName());
        vo.setBrandId(spu.getBrandId());
        vo.setBrandName(spu.getBrandName());
        vo.setDescription(spu.getDescription());
        vo.setMainImage(spu.getMainImage());
        vo.setImages(spu.getImages());
        vo.setUnit(spu.getUnit());
        vo.setKeywords(spu.getKeywords());
        vo.setSales(spu.getSales());
        vo.setMinPrice(spu.getMinPrice());
        vo.setSellerId(spu.getSellerId());
        vo.setStoreId(spu.getStoreId());
        vo.setStatus(spu.getStatus());
        vo.setCreatedAt(spu.getCreatedAt());
        vo.setUpdatedAt(spu.getUpdatedAt());

        // 回填商家详细信息
        if (spu.getSellerId() != null) {
            UserProfileVo seller = userClient.getUserDetail(spu.getSellerId()).getData();
            if (seller != null) {
                vo.setSellerUsername(seller.getUsername());
                vo.setSellerAvatar(seller.getAvatar());
                vo.setSellerRealName(seller.getRealName());
                vo.setSellerPhone(seller.getPhone());
            }
        }

        spuCacheService.setSpuSellerDetail(id, vo);
        return vo;
    }

    /**
     * 获取管理端 SPU 详情（Cache-Aside）
     *
     * <p>TTL 10 分钟。返回 {@link SpuAdminDetailVo}，相比商家视角多
     * {@code isDeleted} 字段。</p>
     */
    @Override
    public SpuAdminDetailVo getSpuAdminDetailById(Long id) {
        SpuAdminDetailVo cached = spuCacheService.getSpuAdminDetail(id);
        if (cached != null) {
            return cached;
        }

        Spu spu = spuMapper.selectById(id);
        if (spu == null) {
            return null;
        }
        fillCategoryAndBrandNames(Collections.singletonList(spu));

        SpuAdminDetailVo vo = new SpuAdminDetailVo();
        vo.setId(spu.getId());
        vo.setName(spu.getName());
        vo.setCategoryId(spu.getCategoryId());
        vo.setCategoryName(spu.getCategoryName());
        vo.setBrandId(spu.getBrandId());
        vo.setBrandName(spu.getBrandName());
        vo.setDescription(spu.getDescription());
        vo.setMainImage(spu.getMainImage());
        vo.setImages(spu.getImages());
        vo.setUnit(spu.getUnit());
        vo.setKeywords(spu.getKeywords());
        vo.setSales(spu.getSales());
        vo.setMinPrice(spu.getMinPrice());
        vo.setSellerId(spu.getSellerId());
        vo.setStoreId(spu.getStoreId());
        vo.setStatus(spu.getStatus());
        vo.setCreatedAt(spu.getCreatedAt());
        vo.setUpdatedAt(spu.getUpdatedAt());
        vo.setIsDeleted(spu.getIsDeleted());

        if (spu.getSellerId() != null) {
            UserProfileVo seller = userClient.getUserDetail(spu.getSellerId()).getData();
            if (seller != null) {
                vo.setSellerUsername(seller.getUsername());
                vo.setSellerAvatar(seller.getAvatar());
                vo.setSellerRealName(seller.getRealName());
                vo.setSellerPhone(seller.getPhone());
            }
        }

        spuCacheService.setSpuAdminDetail(id, vo);
        return vo;
    }

    // ==================== 店铺公开列表 ====================

    /**
     * 根据店铺 ID 分页查询上架 SPU（公开）
     *
     * <p>支持按销量/价格/时间排序，支持价格区间筛选。
     * 仅返回 {@code status=1} 且 {@code is_deleted=false} 的商品。</p>
     */
    @Override
    public List<SpuVo> getPageByStoreId(Long storeId, String keyword, Long categoryId,
                                        BigDecimal minPrice, BigDecimal maxPrice,
                                        String sortBy, String sortOrder,
                                        Integer page, Integer pageSize) {
        Page<Spu> pageParam = new Page<>(page != null ? page : 1, pageSize != null ? pageSize : 10);
        LambdaQueryWrapper<Spu> queryWrapper = buildStoreSpuQuery(storeId, keyword, categoryId, minPrice, maxPrice);

        // 排序逻辑
        if ("sales".equalsIgnoreCase(sortBy)) {
            queryWrapper.orderBy(true, "asc".equalsIgnoreCase(sortOrder), Spu::getSales);
        } else if ("price".equalsIgnoreCase(sortBy)) {
            queryWrapper.orderBy(true, "asc".equalsIgnoreCase(sortOrder), Spu::getMinPrice);
        } else {
            queryWrapper.orderBy(true, !"asc".equalsIgnoreCase(sortOrder), Spu::getCreatedAt);
        }

        IPage<Spu> result = spuMapper.selectPage(pageParam, queryWrapper);
        List<Spu> spuList = result.getRecords();
        fillCategoryAndBrandNames(spuList);

        return spuList.stream()
                .map(this::convertToSpuVO)
                .collect(Collectors.toList());
    }

    /**
     * 统计店铺下 SPU 数量（与 {@link #getPageByStoreId} 条件一致）
     */
    @Override
    public int countByStoreId(Long storeId, String keyword, Long categoryId,
                              BigDecimal minPrice, BigDecimal maxPrice) {
        LambdaQueryWrapper<Spu> queryWrapper = buildStoreSpuQuery(storeId, keyword, categoryId, minPrice, maxPrice);
        return spuMapper.selectCount(queryWrapper).intValue();
    }

    // ==================== 查询条件构建 ====================

    /**
     * 构建通用 SPU 查询条件
     */
    private LambdaQueryWrapper<Spu> buildQueryWrapper(Spu spu) {
        LambdaQueryWrapper<Spu> queryWrapper = new LambdaQueryWrapper<>();
        if (spu == null) {
            return queryWrapper;
        }
        if (spu.getId() != null) {
            queryWrapper.eq(Spu::getId, spu.getId());
        }
        if (spu.getCategoryId() != null) {
            queryWrapper.eq(Spu::getCategoryId, spu.getCategoryId());
        }
        if (spu.getBrandId() != null) {
            queryWrapper.eq(Spu::getBrandId, spu.getBrandId());
        }
        if (StringUtils.hasText(spu.getName())) {
            queryWrapper.like(Spu::getName, spu.getName());
        }
        if (spu.getStatus() != null) {
            queryWrapper.eq(Spu::getStatus, spu.getStatus());
        }
        return queryWrapper;
    }

    /**
     * 构建店铺 SPU 查询条件
     * <p>自动附加：店铺 ID + 上架状态 + 未删除</p>
     */
    private LambdaQueryWrapper<Spu> buildStoreSpuQuery(Long storeId, String keyword,
                                                        Long categoryId,
                                                        BigDecimal minPrice, BigDecimal maxPrice) {
        LambdaQueryWrapper<Spu> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Spu::getStoreId, storeId);
        queryWrapper.eq(Spu::getStatus, 1);
        queryWrapper.eq(Spu::getIsDeleted, false);

        if (categoryId != null) {
            queryWrapper.eq(Spu::getCategoryId, categoryId);
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            queryWrapper.like(Spu::getName, keyword.trim());
        }
        if (minPrice != null) {
            queryWrapper.ge(Spu::getMinPrice, minPrice);
        }
        if (maxPrice != null) {
            queryWrapper.le(Spu::getMinPrice, maxPrice);
        }
        return queryWrapper;
    }

    // ==================== 名称回填 ====================

    /**
     * 批量回填分类名称和品牌名称到 SPU 列表
     * <p>使用 {@code selectBatchIds} 一次查询所有分类/品牌，避免 N+1 问题。</p>
     */
    private void fillCategoryAndBrandNames(List<Spu> spuList) {
        if (spuList == null || spuList.isEmpty()) {
            return;
        }

        Set<Long> categoryIds = spuList.stream()
                .map(Spu::getCategoryId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Set<Long> brandIds = spuList.stream()
                .map(Spu::getBrandId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, String> categoryNameMap = new HashMap<>();
        if (!categoryIds.isEmpty()) {
            List<Category> categories = categoryMapper.selectBatchIds(new ArrayList<>(categoryIds));
            if (categories != null) {
                for (Category c : categories) {
                    if (c.getName() != null) {
                        categoryNameMap.put(c.getId(), c.getName());
                    }
                }
            }
        }

        Map<Long, String> brandNameMap = new HashMap<>();
        if (!brandIds.isEmpty()) {
            List<Brand> brands = brandMapper.selectBatchIds(new ArrayList<>(brandIds));
            if (brands != null) {
                for (Brand b : brands) {
                    if (b.getName() != null) {
                        brandNameMap.put(b.getId(), b.getName());
                    }
                }
            }
        }

        for (Spu spu : spuList) {
            spu.setCategoryName(categoryNameMap.get(spu.getCategoryId()));
            spu.setBrandName(brandNameMap.get(spu.getBrandId()));
        }
    }

    // ==================== VO 转换 ====================

    private SpuVo convertToSpuVO(Spu spu) {
        if (spu == null) return null;
        SpuVo vo = new SpuVo();
        vo.setId(spu.getId());
        vo.setName(spu.getName());
        vo.setCategoryId(spu.getCategoryId());
        vo.setCategoryName(spu.getCategoryName());
        vo.setBrandId(spu.getBrandId());
        vo.setBrandName(spu.getBrandName());
        vo.setDescription(spu.getDescription());
        vo.setMainImage(spu.getMainImage());
        vo.setImages(spu.getImages());
        vo.setUnit(spu.getUnit());
        vo.setKeywords(spu.getKeywords());
        vo.setSales(spu.getSales());
        vo.setMinPrice(spu.getMinPrice());
        return vo;
    }

    private SpuSellerVo convertToSpuSellerVO(Spu spu) {
        if (spu == null) return null;
        SpuSellerVo vo = new SpuSellerVo();
        vo.setId(spu.getId());
        vo.setName(spu.getName());
        vo.setCategoryId(spu.getCategoryId());
        vo.setCategoryName(spu.getCategoryName());
        vo.setBrandId(spu.getBrandId());
        vo.setBrandName(spu.getBrandName());
        vo.setDescription(spu.getDescription());
        vo.setMainImage(spu.getMainImage());
        vo.setImages(spu.getImages());
        vo.setUnit(spu.getUnit());
        vo.setKeywords(spu.getKeywords());
        vo.setSales(spu.getSales());
        vo.setMinPrice(spu.getMinPrice());
        vo.setSellerId(spu.getSellerId());
        vo.setStoreId(spu.getStoreId());
        vo.setStatus(spu.getStatus());
        vo.setCreatedAt(spu.getCreatedAt());
        vo.setUpdatedAt(spu.getUpdatedAt());
        return vo;
    }

    private SpuAdminVo convertToSpuAdminVO(Spu spu) {
        if (spu == null) return null;
        SpuAdminVo vo = new SpuAdminVo();
        vo.setId(spu.getId());
        vo.setName(spu.getName());
        vo.setCategoryId(spu.getCategoryId());
        vo.setCategoryName(spu.getCategoryName());
        vo.setBrandId(spu.getBrandId());
        vo.setBrandName(spu.getBrandName());
        vo.setDescription(spu.getDescription());
        vo.setMainImage(spu.getMainImage());
        vo.setImages(spu.getImages());
        vo.setUnit(spu.getUnit());
        vo.setKeywords(spu.getKeywords());
        vo.setSales(spu.getSales());
        vo.setMinPrice(spu.getMinPrice());
        vo.setSellerId(spu.getSellerId());
        vo.setStoreId(spu.getStoreId());
        vo.setStatus(spu.getStatus());
        vo.setCreatedAt(spu.getCreatedAt());
        vo.setUpdatedAt(spu.getUpdatedAt());
        vo.setIsDeleted(spu.getIsDeleted());
        return vo;
    }
}