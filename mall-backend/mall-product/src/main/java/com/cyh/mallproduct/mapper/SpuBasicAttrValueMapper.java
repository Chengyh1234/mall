package com.cyh.mallproduct.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cyh.mallproduct.entity.SpuBasicAttrValue;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * SPU基本属性值Mapper接口
 */
@Mapper
public interface SpuBasicAttrValueMapper extends BaseMapper<SpuBasicAttrValue> {

    /**
     * 根据SPU ID获取基本属性值列表
     */
    List<SpuBasicAttrValue> getBySpuId(@Param("spuId") Long spuId);
}