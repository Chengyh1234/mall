package com.cyh.mallproduct.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cyh.mallproduct.entity.Attribute;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 属性Mapper接口
 */
@Mapper
public interface AttributeMapper extends BaseMapper<Attribute> {

    /**
     * 根据分类ID获取该分类下的属性列表
     */
    List<Attribute> getByCategoryId(@Param("categoryId") Long categoryId);

    /**
     * 根据分类ID和属性类型获取属性列表
     */
    List<Attribute> getByCategoryIdAndType(@Param("categoryId") Long categoryId, @Param("attrType") Integer attrType);

    /**
     * 获取尚未绑定到指定分类的属性列表
     * 用于运营管理员选择可绑定的属性
     *
     * @param categoryId 分类ID
     * @return 未绑定的属性列表
     */
    List<Attribute> getAvailableByCategoryId(@Param("categoryId") Long categoryId);
}