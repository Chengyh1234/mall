package com.cyh.mallproduct.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cyh.mallcommon.exception.BusinessException;
import com.cyh.mallproduct.dto.CategoryAttributeBindDto;
import com.cyh.mallproduct.dto.CategoryAttributeSortDto;
import com.cyh.mallproduct.entity.Attribute;
import com.cyh.mallproduct.entity.Category;
import com.cyh.mallproduct.entity.CategoryAttribute;
import com.cyh.mallproduct.mapper.AttributeMapper;
import com.cyh.mallproduct.mapper.CategoryAttributeMapper;
import com.cyh.mallproduct.mapper.CategoryMapper;
import com.cyh.mallproduct.service.CategoryAttributeManageService;
import com.cyh.mallproduct.vo.AttributeAvailableVo;
import com.cyh.mallproduct.vo.CategoryAttributeBoundVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 分类-属性绑定管理 Service 实现类
 *
 * 提供分类与属性绑定关系的增删改查功能，
 * 包含解绑前的 SPU 引用检查
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryAttributeManageServiceImpl implements CategoryAttributeManageService {

    private final CategoryAttributeMapper categoryAttributeMapper;
    private final AttributeMapper attributeMapper;
    private final CategoryMapper categoryMapper;

    @Override
    public List<CategoryAttributeBoundVo> getBoundAttributes(Long categoryId) {
        log.info("获取分类已绑定的属性列表, categoryId={}", categoryId);

        // 查询该分类下的所有绑定记录
        List<CategoryAttribute> bindings = categoryAttributeMapper.selectList(
                new LambdaQueryWrapper<CategoryAttribute>()
                        .eq(CategoryAttribute::getCategoryId, categoryId)
                        .orderByAsc(CategoryAttribute::getSort)
        );

        if (bindings.isEmpty()) {
            return Collections.emptyList();
        }

        // 批量查询绑定的属性信息
        List<Long> attrIds = bindings.stream()
                .map(CategoryAttribute::getAttrId)
                .collect(Collectors.toList());
        List<Attribute> attributes = attributeMapper.selectList(
                new LambdaQueryWrapper<Attribute>().in(Attribute::getId, attrIds)
        );
        Map<Long, Attribute> attrMap = attributes.stream()
                .collect(Collectors.toMap(Attribute::getId, a -> a));

        // 组装 VO，逐个检查 SPU 引用状态
        return bindings.stream().map(binding -> {
            Attribute attr = attrMap.get(binding.getAttrId());
            CategoryAttributeBoundVo vo = new CategoryAttributeBoundVo();
            vo.setId(binding.getId());
            vo.setCategoryId(binding.getCategoryId());
            vo.setAttrId(binding.getAttrId());
            vo.setSort(binding.getSort());
            if (attr != null) {
                vo.setAttrName(attr.getName());
                vo.setAttrType(attr.getAttrType());
            }
            // 检查该分类下的 SPU 是否使用了该属性
            boolean hasUsage = checkAttributeUsage(binding.getCategoryId(), binding.getAttrId());
            vo.setHasSpuUsage(hasUsage);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public List<AttributeAvailableVo> getAvailableAttributes(Long categoryId) {
        log.info("获取分类可绑定的属性列表, categoryId={}", categoryId);
        List<Attribute> attributes = attributeMapper.getAvailableByCategoryId(categoryId);
        return attributes.stream().map(this::toAvailableAttributeVo).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long bindAttribute(CategoryAttributeBindDto dto) {
        log.info("绑定属性到分类: categoryId={}, attrId={}", dto.getCategoryId(), dto.getAttrId());

        // 校验：只有叶子分类（没有子分类的分类）才能绑定属性
        // 因为 SPU 只能挂在叶子分类下，非叶子分类绑定属性无实际意义
        Long childCount = categoryMapper.selectCount(
                new LambdaQueryWrapper<Category>()
                        .eq(Category::getParentId, dto.getCategoryId())
                        .eq(Category::getIsDeleted, false)
        );
        if (childCount != null && childCount > 0) {
            throw new BusinessException("该分类下有子分类，请选择叶子分类进行属性绑定");
        }

        // 检查该绑定是否已存在
        CategoryAttribute existing = categoryAttributeMapper.selectOne(
                new LambdaQueryWrapper<CategoryAttribute>()
                        .eq(CategoryAttribute::getCategoryId, dto.getCategoryId())
                        .eq(CategoryAttribute::getAttrId, dto.getAttrId())
        );
        if (existing != null) {
            throw new BusinessException("该属性已绑定到此分类，请勿重复绑定");
        }

        // 新增绑定记录
        CategoryAttribute binding = new CategoryAttribute();
        binding.setCategoryId(dto.getCategoryId());
        binding.setAttrId(dto.getAttrId());
        binding.setSort(dto.getSort() != null ? dto.getSort() : 0);
        binding.setCreatedAt(LocalDateTime.now());

        categoryAttributeMapper.insert(binding);
        log.info("属性绑定成功, id={}", binding.getId());
        return binding.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateSort(Long id, CategoryAttributeSortDto dto) {
        log.info("修改绑定排序: id={}, sort={}", id, dto.getSort());

        // 检查绑定记录是否存在
        CategoryAttribute existing = categoryAttributeMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("绑定记录不存在");
        }

        // 更新排序（排序调整不影响已有 SPU 数据，无需引用检查）
        CategoryAttribute update = new CategoryAttribute();
        update.setId(id);
        update.setSort(dto.getSort());
        update.setUpdatedAt(LocalDateTime.now());

        int rows = categoryAttributeMapper.updateById(update);
        return rows > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean unbindAttribute(Long id) {
        log.info("解绑属性: id={}", id);

        // 检查绑定记录是否存在
        CategoryAttribute binding = categoryAttributeMapper.selectById(id);
        if (binding == null) {
            throw new BusinessException("绑定记录不存在");
        }

        // 检查该分类下的 SPU 是否已使用了该属性
        boolean hasUsage = checkAttributeUsage(binding.getCategoryId(), binding.getAttrId());
        if (hasUsage) {
            throw new BusinessException("该分类下的 SPU 已使用了该属性，无法解绑。请先移除相关 SPU 的属性绑定后再操作");
        }

        // 执行解绑
        int rows = categoryAttributeMapper.deleteById(id);
        return rows > 0;
    }

    // ==================== 私有方法 ====================

    /**
     * 检查指定分类下的 SPU 是否已使用了该属性
     * 分别检查 spu_basic_attr_values（基本属性）和 spu_sale_attr_choice（销售属性）两张引用表
     *
     * @param categoryId 分类ID
     * @param attrId     属性ID
     * @return true=已被SPU引用，false=未被引用
     */
    private boolean checkAttributeUsage(Long categoryId, Long attrId) {
        int basicUsage = categoryAttributeMapper.countSpuBasicAttrUsage(categoryId, attrId);
        if (basicUsage > 0) {
            return true;
        }

        int saleUsage = categoryAttributeMapper.countSpuSaleAttrUsage(categoryId, attrId);
        return saleUsage > 0;
    }

    /**
     * 将 Attribute 实体转换为 AttributeAvailableVo
     */
    private AttributeAvailableVo toAvailableAttributeVo(Attribute attribute) {
        AttributeAvailableVo vo = new AttributeAvailableVo();
        vo.setId(attribute.getId());
        vo.setName(attribute.getName());
        vo.setAttrType(attribute.getAttrType());
        vo.setSort(attribute.getSort());
        return vo;
    }
}