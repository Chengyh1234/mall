package com.cyh.mallproduct.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cyh.mallproduct.entity.Attribute;
import com.cyh.mallproduct.entity.CategoryAttribute;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 分类-属性关联 Mapper 接口
 *
 * 提供分类与属性绑定关系的数据访问
 */
@Mapper
public interface CategoryAttributeMapper extends BaseMapper<CategoryAttribute> {

    /**
     * 获取指定分类已绑定的属性列表（含属性信息）
     *
     * @param categoryId 分类ID
     * @return 已绑定的属性列表
     */
    List<Attribute> getBoundAttributes(@Param("categoryId") Long categoryId);

    /**
     * 统计指定分类下的 SPU 在基本属性中使用了该属性的次数
     *
     * @param categoryId 分类ID
     * @param attrId     属性ID
     * @return 引用次数
     */
    int countSpuBasicAttrUsage(@Param("categoryId") Long categoryId, @Param("attrId") Long attrId);

    /**
     * 统计指定分类下的 SPU 在销售属性中使用了该属性的次数
     *
     * @param categoryId 分类ID
     * @param attrId     属性ID
     * @return 引用次数
     */
    int countSpuSaleAttrUsage(@Param("categoryId") Long categoryId, @Param("attrId") Long attrId);
}