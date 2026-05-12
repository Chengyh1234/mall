package com.cyh.mallportal.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cyh.mallportal.entity.Spu;
import com.cyh.mallportal.mapper.SpuMapper;
import com.cyh.mallportal.service.SpuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 商品SPU服务实现类
 * 实现商品的新增、删除、修改、查询等功能
 */
@Service
public class SpuServiceImpl implements SpuService {

    /**
     * 商品Mapper接口
     */
    @Autowired
    private SpuMapper spuMapper;

    /**
     * 新增商品
     * @param spu 商品信息
     * @return 新增的商品ID，失败返回null
     */
    @Override
    public Long add(Spu spu) {
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
        return result > 0 ? spu.getId() : null;
    }

    /**
     * 根据ID删除商品
     * @param id 商品ID
     * @return 删除成功返回true，失败返回false
     */
    @Override
    public boolean delete(Long id) {
        return spuMapper.deleteById(id) > 0;
    }

    /**
     * 更新商品信息
     * @param spu 商品信息（需包含ID）
     * @return 更新成功返回true，失败返回false
     */
    @Override
    public boolean update(Spu spu) {
        // 更新时设置更新时间
        spu.setUpdatedAt(LocalDateTime.now());
        return spuMapper.updateById(spu) > 0;
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
     * 分页查询商品
     * @param spu 查询条件（支持id、categoryId、brandId、name、status）
     * @param page 页码，默认第1页
     * @param pageSize 每页数量，默认10条
     * @return 商品列表，按创建时间倒序排列
     */
    @Override
    public List<Spu> getPage(Spu spu, Integer page, Integer pageSize) {
        // 创建分页参数
        Page<Spu> pageParam = new Page<>(page != null ? page : 1, pageSize != null ? pageSize : 10);
        // 构建查询条件
        LambdaQueryWrapper<Spu> queryWrapper = buildQueryWrapper(spu);
        // 按创建时间倒序排列
        queryWrapper.orderByDesc(Spu::getCreatedAt);
        // 执行分页查询
        IPage<Spu> result = spuMapper.selectPage(pageParam, queryWrapper);
        return result.getRecords();
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
     * 分页搜索商品（支持多字段模糊搜索：商品名称、分类名称、品牌名称）
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
        int offset = (page != null && page > 0 ? page - 1 : 0) * (pageSize != null ? pageSize : 10);
        int limit = pageSize != null ? pageSize : 10;
        return spuMapper.searchByKeyword(categoryIds, keyword, brandId, offset, limit);
    }

    /**
     * 统计搜索结果数量
     *
     * @param categoryIds 分类ID列表（可选）
     * @param keyword 搜索关键字（可选）
     * @param brandId 品牌ID（可选）
     * @return 商品数量
     */
    @Override
    public int countSearchByKeyword(List<Long> categoryIds, String keyword, Long brandId) {
        return spuMapper.countSearchByKeyword(categoryIds, keyword, brandId);
    }
}
