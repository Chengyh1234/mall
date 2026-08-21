package com.cyh.mallproduct.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cyh.mallproduct.entity.SpuSaleAttrCustomValue;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * SPU销售属性自定义值Mapper接口
 */
@Mapper
public interface SpuSaleAttrCustomValueMapper extends BaseMapper<SpuSaleAttrCustomValue> {

    /**
     * 根据SPU ID和属性ID获取自定义值列表
     */
    List<SpuSaleAttrCustomValue> getBySpuIdAndAttrId(@Param("spuId") Long spuId,
                                                     @Param("attrId") Long attrId);

    /**
     * 根据SPU ID获取所有自定义值列表
     */
    List<SpuSaleAttrCustomValue> getBySpuId(@Param("spuId") Long spuId);

    /**
     * 根据ID列表批量查询自定义值
     */
    List<SpuSaleAttrCustomValue> getByIds(@Param("ids") List<Long> ids);

    /**
     * 根据SPU ID删除所有自定义值
     */
    int deleteBySpuId(@Param("spuId") Long spuId);

    /**
     * 根据SPU ID和属性ID删除所有自定义值
     */
    int deleteBySpuIdAndAttrId(@Param("spuId") Long spuId,
                               @Param("attrId") Long attrId);
}