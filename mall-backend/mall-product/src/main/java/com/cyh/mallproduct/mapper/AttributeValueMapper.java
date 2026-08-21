package com.cyh.mallproduct.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cyh.mallproduct.entity.AttributeValue;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 属性值Mapper接口
 */
@Mapper
public interface AttributeValueMapper extends BaseMapper<AttributeValue> {

    /**
     * 根据属性ID获取属性值列表
     */
    List<AttributeValue> getByAttrId(@Param("attrId") Long attrId);

    /**
     * 根据多个属性ID获取属性值列表
     */
    List<AttributeValue> getByAttrIds(@Param("attrIds") List<Long> attrIds);

    // ==================== 引用检查（用于属性值删除/修改前的校验） ====================

    /**
     * 统计该属性值被 SPU 基本属性引用的次数
     * 如果返回值 > 0，说明有 SPU 使用了该属性值，禁止修改或删除
     *
     * @param attrValueId 属性值ID
     * @return 引用次数
     */
    int countUsedInSpuBasic(@Param("attrValueId") Long attrValueId);

    /**
     * 统计该属性值被 SPU 销售属性（JSON字段）引用的次数
     * 检查 spu_sale_attr_choice.selected_values JSON 数组中是否包含该值ID
     *
     * @param attrValueId 属性值ID
     * @return 引用次数
     */
    int countUsedInSpuSale(@Param("attrValueId") Long attrValueId);

    /**
     * 统计该属性值被 SKU 销售属性引用的次数
     * 如果返回值 > 0，说明有 SKU 使用了该属性值，禁止修改或删除
     *
     * @param attrValueId 属性值ID
     * @return 引用次数
     */
    int countUsedInSkuSale(@Param("attrValueId") Long attrValueId);
}