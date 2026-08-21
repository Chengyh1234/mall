package com.cyh.mallproduct.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cyh.mallproduct.entity.SpuSaleAttrChoice;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * SPU销售属性选择Mapper接口
 */
@Mapper
public interface SpuSaleAttrChoiceMapper extends BaseMapper<SpuSaleAttrChoice> {

    /**
     * 根据SPU ID获取销售属性选择列表
     */
    List<SpuSaleAttrChoice> getBySpuId(@Param("spuId") Long spuId);
}