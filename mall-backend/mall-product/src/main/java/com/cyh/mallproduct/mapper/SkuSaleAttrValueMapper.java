package com.cyh.mallproduct.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cyh.mallproduct.entity.SkuSaleAttrValue;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * SKU销售属性值关联Mapper接口
 * 
 * <p>用于操作 sku_sale_attr_values 表，管理SKU与销售属性值的关联关系
 */
@Mapper
public interface SkuSaleAttrValueMapper extends BaseMapper<SkuSaleAttrValue> {

    /**
     * 根据SKU ID获取关联的销售属性值列表
     * 
     * @param skuId SKU ID
     * @return 销售属性值关联列表
     */
    List<SkuSaleAttrValue> getBySkuId(@Param("skuId") Long skuId);

    /**
     * 根据SKU ID列表批量获取关联的销售属性值
     * 
     * @param skuIds SKU ID列表
     * @return 销售属性值关联列表
     */
    List<SkuSaleAttrValue> getBySkuIds(@Param("skuIds") List<Long> skuIds);

    /**
     * 根据SKU ID删除关联的销售属性值
     * 
     * @param skuId SKU ID
     * @return 删除的记录数
     */
    //int deleteBySkuId(@Param("skuId") Long skuId);

    /**
     * 根据属性值ID获取关联的SKU列表
     * 
     * @param attrValueId 属性值ID
     * @return 销售属性值关联列表
     */
    List<SkuSaleAttrValue> getByAttrValueId(@Param("attrValueId") Long attrValueId);
}