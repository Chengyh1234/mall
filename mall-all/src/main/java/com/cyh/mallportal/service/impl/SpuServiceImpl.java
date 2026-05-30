package com.cyh.mallportal.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cyh.mallcommon.exception.BusinessException;
import com.cyh.mallportal.entity.*;
import com.cyh.mallportal.mapper.*;
import com.cyh.mallportal.service.SpuCacheService;
import com.cyh.mallportal.service.SpuService;
import com.cyh.mallportal.vo.SpuDetailVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 商品SPU服务实现类
 * 实现商品的新增、删除、修改、查询等功能
 */
@Slf4j
@Service
public class SpuServiceImpl implements SpuService {

    /**
     * 商品Mapper接口
     */
    @Autowired
    private SpuMapper spuMapper;

    /**
     * 用户Mapper接口
     */
    @Autowired
    private UserMapper userMapper;

    /**
     * SPU缓存服务
     */
    @Autowired
    private SpuCacheService spuCacheService;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private BrandMapper brandMapper;

    @Autowired
    private SpuBasicAttrValueMapper spuBasicAttrValueMapper;

    @Autowired
    private SpuSaleAttrChoiceMapper spuSaleAttrChoiceMapper;

    @Autowired
    private SkuMapper skuMapper;

    /**
     * 新增商品
     * @param spu 商品信息
     * @return 新增的商品ID，失败返回null
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long add(Spu spu) {
        // 校验同一商家下商品名称唯一性
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
        // 设置创建时间和更新时间
        spu.setCreatedAt(LocalDateTime.now());
        spu.setUpdatedAt(LocalDateTime.now());
        // 默认上架状态为上架(1)
        if (spu.getStatus() == null) {
            spu.setStatus(1);
        }
        // 默认销量为0
        if (spu.getSales() == null) {
            spu.setSales(0);
        }
        // 执行插入操作
        int result = spuMapper.insert(spu);
        if (result > 0) {
            // 清除所有SPU缓存
            spuCacheService.clearAllSpuCache();
            log.info("新增商品后清除SPU缓存");
        }
        return result > 0 ? spu.getId() : null;
    }

    /**
     * 根据ID删除商品（逻辑删除）
     * @param id 商品ID
     * @return 删除成功返回true，失败返回false
     */
    @Override
    public boolean delete(Long id) {
        int result = spuMapper.deleteById(id);
        if (result > 0) {
            // 清除所有SPU缓存
            spuCacheService.clearAllSpuCache();
            log.info("删除商品后清除SPU缓存");
        }
        return result > 0;
    }

    /**
     * 恢复被逻辑删除的商品（设置 is_deleted=0）
     * @param id 商品ID
     * @return 恢复成功返回true，失败返回false
     */
    @Override
    public boolean restore(Long id) {
        // 使用UpdateWrapper绕过@TableLogic过滤
        UpdateWrapper<Spu> wrapper = new UpdateWrapper<>();
        wrapper.eq("id", id);
        wrapper.set("is_deleted", 0);
        int result = spuMapper.update(null, wrapper);
        if (result > 0) {
            spuCacheService.clearAllSpuCache();
            log.info("恢复商品后清除SPU缓存");
        }
        return result > 0;
    }

    /**
     * 更新商品信息
     * @param spu 商品信息（需包含ID）
     * @return 更新成功返回true，失败返回false
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean update(Spu spu) {
        Spu oldSpu = spuMapper.selectById(spu.getId());
        if (oldSpu == null) {
            throw new BusinessException("商品不存在");
        }

        // 校验同一商家下商品名称唯一性（排除当前商品自身）
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

        if (spu.getCategoryId() != null && !spu.getCategoryId().equals(oldSpu.getCategoryId())) {
            Long childCount = categoryMapper.selectCount(
                    new LambdaQueryWrapper<Category>()
                            .eq(Category::getParentId, spu.getCategoryId())
                            .eq(Category::getIsDeleted, false)
            );
            if (childCount > 0) {
                throw new BusinessException("该分类下有子分类，请选择叶子分类");
            }

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
        }

        spu.setUpdatedAt(LocalDateTime.now());
        int result = spuMapper.updateById(spu);
        if (result > 0) {
            spuCacheService.clearAllSpuCache();
            log.info("更新商品后清除SPU缓存");
        }
        return result > 0;
    }

    /**
     * 根据ID获取商品详情
     * @param id 商品ID
     * @return 商品信息，不存在返回null
     */
    @Override
    public Spu getById(Long id) {
        return spuMapper.selectById(id);
    }

    /**
     * 获取商品列表（不分页）
     * @param spu 查询条件（支持id、categoryId、brandId、name、status）
     * @return 商品列表，按创建时间倒序排列
     */
    @Override
    public List<Spu> getList(Spu spu) {
        // 构建查询条件
        LambdaQueryWrapper<Spu> queryWrapper = buildQueryWrapper(spu);
        // 按创建时间倒序排列
        queryWrapper.orderByDesc(Spu::getCreatedAt);
        return spuMapper.selectList(queryWrapper);
    }

    /**
     * 分页查询商品（带缓存）
     * @param spu 查询条件（支持id、categoryId、brandId、name、status）
     * @param page 页码，默认第1页
     * @param pageSize 每页数量，默认10条
     * @return 商品列表，按创建时间倒序排列
     */
    @Override
    public List<Spu> getPage(Spu spu, Integer page, Integer pageSize) {
        int pageNum = page != null ? page : 1;
        int pageSizeNum = pageSize != null ? pageSize : 10;
        
        // 生成缓存键
        String cacheKey = spuCacheService.generateCacheKey(
                spu != null ? spu.getCategoryId() : null,
                spu != null ? spu.getBrandId() : null,
                spu != null ? spu.getName() : null,
                spu != null ? spu.getStatus() : null,
                pageNum,
                pageSizeNum
        );
        
        // 尝试从缓存获取
        List<Spu> cachedList = spuCacheService.getSpuList(cacheKey);
        if (cachedList != null) {
            log.debug("从缓存获取分页商品列表: {}", cacheKey);
            return cachedList;
        }
        
        // 缓存不存在，从数据库查询
        Page<Spu> pageParam = new Page<>(pageNum, pageSizeNum);
        LambdaQueryWrapper<Spu> queryWrapper = buildQueryWrapper(spu);
        queryWrapper.orderByDesc(Spu::getCreatedAt);
        IPage<Spu> result = spuMapper.selectPage(pageParam, queryWrapper);
        List<Spu> records = result.getRecords();
        
        // 将结果放入缓存
        spuCacheService.setSpuList(cacheKey, records);
        log.debug("从数据库查询并缓存分页商品列表: {}", cacheKey);
        
        return records;
    }

    /**
     * 分页查询商品（支持分类及其子分类）
     * 当传入categoryIds时，会查询这些分类ID关联的所有商品
     * @param spu 查询条件（支持id、brandId、name、status）
     * @param page 页码
     * @param pageSize 每页数量
     * @param categoryIds 分类ID列表（包含分类及其子分类）
     * @return 商品列表，按创建时间倒序排列
     */
    @Override
    public List<Spu> getPageByCategoryIds(Spu spu, Integer page, Integer pageSize, List<Long> categoryIds) {
        // 创建分页参数
        Page<Spu> pageParam = new Page<>(page != null ? page : 1, pageSize != null ? pageSize : 10);
        // 构建查询条件
        LambdaQueryWrapper<Spu> queryWrapper = new LambdaQueryWrapper<>();

        // 如果有分类ID列表，使用IN查询
        if (categoryIds != null && !categoryIds.isEmpty()) {
            queryWrapper.in(Spu::getCategoryId, categoryIds);
        }

        // 按ID精确查询
        if (spu != null && spu.getId() != null) {
            queryWrapper.eq(Spu::getId, spu.getId());
        }
        // 按品牌ID精确查询
        if (spu != null && spu.getBrandId() != null) {
            queryWrapper.eq(Spu::getBrandId, spu.getBrandId());
        }
        // 按商品名称模糊查询
        if (spu != null && StringUtils.hasText(spu.getName())) {
            queryWrapper.like(Spu::getName, spu.getName());
        }

        // 按创建时间倒序排列
        queryWrapper.orderByDesc(Spu::getCreatedAt);

        // 执行分页查询
        IPage<Spu> result = spuMapper.selectPage(pageParam, queryWrapper);
        return result.getRecords();
    }

    /**
     * 构建查询条件
     * @param spu 查询条件对象
     * @return LambdaQueryWrapper查询构造器
     */
    private LambdaQueryWrapper<Spu> buildQueryWrapper(Spu spu) {
        LambdaQueryWrapper<Spu> queryWrapper = new LambdaQueryWrapper<>();
        // 如果查询条件为空，直接返回空条件
        if (spu == null) {
            return queryWrapper;
        }
        // 按ID精确查询
        if (spu.getId() != null) {
            queryWrapper.eq(Spu::getId, spu.getId());
        }
        // 按分类ID精确查询
        if (spu.getCategoryId() != null) {
            queryWrapper.eq(Spu::getCategoryId, spu.getCategoryId());
        }
        // 按品牌ID精确查询
        if (spu.getBrandId() != null) {
            queryWrapper.eq(Spu::getBrandId, spu.getBrandId());
        }
        // 按商品名称模糊查询
        if (StringUtils.hasText(spu.getName())) {
            queryWrapper.like(Spu::getName, spu.getName());
        }
        return queryWrapper;
    }

    /**
     * 分页搜索商品（支持多字段模糊搜索：商品名称、分类名称、品牌名称，带缓存）
     *
     * @param categoryIds 分类ID列表（可选，包含分类及其子分类）
     * @param keyword 搜索关键字（可选，匹配商品名称、分类名称、品牌名称）
     * @param brandId 品牌ID（可选）
     * @param page 页码
     * @param pageSize 每页数量
     * @return 商品列表
     */
    @Override
    public List<Spu> searchByKeyword(List<Long> categoryIds, String keyword, Long brandId, Integer page, Integer pageSize) {
        int pageNum = page != null && page > 0 ? page : 1;
        int pageSizeNum = pageSize != null ? pageSize : 10;
        
        // 如果有分类ID列表，取第一个（用于生成缓存键）
        Long firstCategoryId = (categoryIds != null && !categoryIds.isEmpty()) ? categoryIds.get(0) : null;
        
        // 生成缓存键
        String cacheKey = spuCacheService.generateCacheKey(firstCategoryId, brandId, keyword, null, pageNum, pageSizeNum);
        
        // 尝试从缓存获取
        List<Spu> cachedList = spuCacheService.getSpuList(cacheKey);
        if (cachedList != null) {
            log.debug("从缓存获取搜索商品列表: {}", cacheKey);
            return cachedList;
        }
        
        // 缓存不存在，从数据库查询
        int offset = (pageNum - 1) * pageSizeNum;
        int limit = pageSizeNum;
        List<Spu> records = spuMapper.searchByKeyword(categoryIds, keyword, brandId, offset, limit);
        
        // 将结果放入缓存
        spuCacheService.setSpuList(cacheKey, records);
        log.debug("从数据库查询并缓存搜索商品列表: {}", cacheKey);
        
        return records;
    }

    /**
     * 统计搜索结果数量（带缓存）
     *
     * @param categoryIds 分类ID列表（可选）
     * @param keyword 搜索关键字（可选）
     * @param brandId 品牌ID（可选）
     * @return 商品数量
     */
    @Override
    public int countSearchByKeyword(List<Long> categoryIds, String keyword, Long brandId) {
        // 如果有分类ID列表，取第一个（用于生成缓存键）
        Long firstCategoryId = (categoryIds != null && !categoryIds.isEmpty()) ? categoryIds.get(0) : null;
        
        // 生成总数缓存键
        String countKey = spuCacheService.generateCountKey(firstCategoryId, brandId, keyword, null);
        
        // 尝试从缓存获取
        Integer cachedCount = spuCacheService.getSpuCount(countKey);
        if (cachedCount != null) {
            log.debug("从缓存获取搜索商品总数: {}", countKey);
            return cachedCount;
        }
        
        // 缓存不存在，从数据库查询
        int count = spuMapper.countSearchByKeyword(categoryIds, keyword, brandId);
        
        // 将结果放入缓存
        spuCacheService.setSpuCount(countKey, count);
        log.debug("从数据库查询并缓存搜索商品总数: {} = {}", countKey, count);
        
        return count;
    }

    /**
     * 根据商家ID获取商品列表（不分页）
     * 用于商家管理自己的商品
     * @param sellerId 商家ID
     * @return 商品列表
     */
    @Override
    public List<Spu> getListBySellerId(Long sellerId) {
        LambdaQueryWrapper<Spu> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Spu::getSellerId, sellerId);
        queryWrapper.orderByDesc(Spu::getCreatedAt);
        return spuMapper.selectList(queryWrapper);
    }

    /**
     * 根据商家ID分页获取商品列表
     * 包含分类名称和品牌名称回填，用于商家管理自己的商品
     * @param sellerId 商家ID
     * @param status 状态（可选，1-上架 0-下架）
     * @param keyword 关键字（可选，按商品名称模糊搜索）
     * @param page 页码
     * @param pageSize 每页数量
     * @return 商品列表（含 categoryName、brandName）
     */
    @Override
    public List<Spu> getPageBySellerId(Long sellerId, Integer status, String keyword, Integer page, Integer pageSize) {
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

        // 收集所有分类ID和品牌ID，批量查询名称后回填到 Spu 的 transient 字段
        fillCategoryAndBrandNames(spuList);

        return spuList;
    }

    /**
     * 批量查询分类名称和品牌名称，回填到 Spu 列表中
     * 使用 MyBatis-Plus 的 selectBatchIds 一次查询所有分类/品牌，避免 N+1 问题
     *
     * @param spuList 商品列表
     */
    private void fillCategoryAndBrandNames(List<Spu> spuList) {
        if (spuList == null || spuList.isEmpty()) {
            return;
        }

        // 收集所有分类ID（去重、去空）
        Set<Long> categoryIds = spuList.stream()
                .map(Spu::getCategoryId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // 收集所有品牌ID（去重、去空）
        Set<Long> brandIds = spuList.stream()
                .map(Spu::getBrandId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // 批量查询分类，构建 ID→名称 映射
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

        // 批量查询品牌，构建 ID→名称 映射
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

        // 回填分类名称和品牌名称到每个 Spu
        for (Spu spu : spuList) {
            spu.setCategoryName(categoryNameMap.get(spu.getCategoryId()));
            spu.setBrandName(brandNameMap.get(spu.getBrandId()));
        }
    }

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
        long count = spuMapper.selectCount(queryWrapper);
        return (int) count;
    }

    /**
     * 根据商家ID获取商品详情
     * 用于商家查看自己商品的详情
     * @param id 商品ID
     * @param sellerId 商家ID（用于权限校验）
     * @return 商品信息，不存在返回null
     */
    @Override
    public Spu getByIdAndSellerId(Long id, Long sellerId) {
        LambdaQueryWrapper<Spu> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Spu::getId, id);
        queryWrapper.eq(Spu::getSellerId, sellerId);
        return spuMapper.selectOne(queryWrapper);
    }

    /**
     * 获取商品详情（包含商家信息）
     * 用于前端展示商品详情页
     * @param id 商品ID
     * @return 商品详情VO，包含商品信息和商家信息
     */
    @Override
    public SpuDetailVo getSpuDetailById(Long id) {
        Spu spu = spuMapper.selectById(id);
        if (spu == null) {
            return null;
        }
        SpuDetailVo vo = new SpuDetailVo();
        vo.setSpu(spu);
        vo.setSellerId(spu.getSellerId());

        if (spu.getSellerId() != null) {
            User seller = userMapper.selectById(spu.getSellerId());
            if (seller != null) {
                vo.setSellerUsername(seller.getUsername());
                vo.setSellerAvatar(seller.getAvatar());
                vo.setSellerRealName(seller.getRealName());
                vo.setSellerPhone(seller.getPhone());
            }
        }
        return vo;
    }
}