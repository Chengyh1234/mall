package com.cyh.mallportal.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cyh.mallportal.entity.AttributeValue;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 属性值Mapper接口
 */
@Mapper
public interface AttributeValueMapper extends BaseMapper<AttributeValue> {

    /**
     * 根据属性ID获取属性值列表
     */
    List<AttributeValue> getByAttrId(Long attrId);

    /**
     * 根据多个属性ID获取属性值列表
     */
    List<AttributeValue> getByAttrIds(List<Long> attrIds);
}
