package com.cyh.mallportal.service;

import com.cyh.mallportal.dto.SkuBatchCreateDto;
import com.cyh.mallportal.dto.SkuSaleAttrBindDto;
import com.cyh.mallportal.dto.SkuUpdateDto;
import com.cyh.mallportal.entity.SkuSaleAttrValue;
import com.cyh.mallportal.vo.SkuAttrVo;
import com.cyh.mallportal.vo.SkuAvailableAttrVo;

import java.util.List;
import java.util.Map;

/**
 * SKU属性管理服务接口
 * 提供商家操作SKU销售属性的功能
 */
public interface SkuAttrService {

    /**
     * 绑定SKU销售属性
     * 商家为SKU绑定销售属性值（如颜色、内存、存储等）
     *
     * @param dto       绑定信息
     * @param sellerId  当前商家ID
     * @return 成功绑定的记录数
     */
    int bindSaleAttr(SkuSaleAttrBindDto dto, Long sellerId);

    /**
     * 批量绑定SKU销售属性
     *
     * @param dtoList   绑定信息列表
     * @param sellerId  当前商家ID
     * @return 成功绑定的记录数
     */
    int batchBindSaleAttr(List<SkuSaleAttrBindDto> dtoList, Long sellerId);

    /**
     * 更新SKU销售属性绑定
     * 先删除原有绑定，再添加新的绑定
     *
     * @param skuId        SKU ID
     * @param attrValueIds 新的属性值ID列表
     * @param sellerId     当前商家ID
     * @return 是否更新成功
     */
    boolean updateSaleAttr(Long skuId, List<Long> attrValueIds, Long sellerId);

    /**
     * 删除SKU销售属性绑定
     *
     * @param id        记录ID
     * @param sellerId  当前商家ID
     * @return 是否删除成功
     */
    boolean deleteSaleAttr(Long id, Long sellerId);

    /**
     * 删除SKU的所有销售属性绑定
     *
     * @param skuId     SKU ID
     * @param sellerId  当前商家ID
     * @return 删除的记录数
     */
    int deleteAllSaleAttrsBySkuId(Long skuId, Long sellerId);

    /**
     * 根据SKU ID获取销售属性列表
     *
     * @param skuId SKU ID
     * @return 销售属性值列表
     */
    List<SkuSaleAttrValue> getSaleAttrsBySkuId(Long skuId);

    /**
     * 获取SKU的销售属性详情
     *
     * @param skuId SKU ID
     * @return SKU销售属性详情
     */
    SkuAttrVo getSkuAttrDetail(Long skuId);

    /**
     * 校验SKU属性组合是否合法
     * 检查SKU绑定的属性值是否都属于该SPU选择的销售属性
     *
     * @param spuId        SPU ID
     * @param attrValueIds 属性值ID列表
     * @return 是否合法
     */
    boolean validateAttrCombination(Long spuId, List<Long> attrValueIds);

    /**
     * 获取SKU可选择的销售属性值列表
     * 根据SPU ID，获取该SPU选择的销售属性及可选属性值，供SKU创建时选择
     *
     * @param spuId SPU ID
     * @return 可用属性列表
     */
    SkuAvailableAttrVo getAvailableAttrsBySpuId(Long spuId);

    /**
     * 批量创建SKU并绑定销售属性（同SPU）
     * 所有SKU必须在同一个SPU下，且属性值组合不能与已有SKU重复
     *
     * @param dtoList  创建参数列表（所有元素必须使用同一个spuId）
     * @param sellerId 当前商家ID
     * @return 创建结果映射（skuId -> 绑定的属性值数量）
     */
    Map<Long, Integer> batchCreateSkuWithAttrs(List<SkuBatchCreateDto> dtoList, Long sellerId);

    /**
     * 创建SKU并绑定销售属性
     * 一步完成单个SKU的新增 + 销售属性绑定
     *
     * @param dto      创建参数
     * @param sellerId 当前商家ID
     * @return 新创建的SKU ID
     */
    Long createSkuWithAttrs(SkuBatchCreateDto dto, Long sellerId);

    /**
     * 更新SKU基本信息（不修改销售属性）
     *
     * @param dto      更新参数
     * @param sellerId 当前商家ID
     * @return 是否更新成功
     */
    boolean updateSkuWithAttrs(SkuUpdateDto dto, Long sellerId);

    /**
     * 批量更新SKU基本信息（不修改销售属性）
     *
     * @param dtoList  更新参数列表
     * @param sellerId 当前商家ID
     * @return 成功更新的SKU数量
     */
    int batchUpdateSkuWithAttrs(List<SkuUpdateDto> dtoList, Long sellerId);
}
