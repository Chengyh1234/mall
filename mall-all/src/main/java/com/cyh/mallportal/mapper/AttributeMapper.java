package com.cyh.mallportal.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cyh.mallportal.entity.Attribute;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 属性Mapper接口
 */
@Mapper
public interface AttributeMapper extends BaseMapper<Attribute> {

    /**
     * 根据分类ID获取该分类下的属性列表
     */
    List<Attribute> getByCategoryId(Long categoryId);

    /**
     * 根据分类ID和属性类型获取属性列表
     */
    List<Attribute> getByCategoryIdAndType(Long categoryId, Integer attrType);
}
