package com.cyh.mallportal.service;

import com.cyh.mallportal.dto.SpuAttrFullBindDto;
import com.cyh.mallportal.dto.SpuBasicAttrBindDto;
import com.cyh.mallportal.dto.SpuSaleAttrBindDto;
import com.cyh.mallportal.entity.SpuBasicAttrValue;
import com.cyh.mallportal.entity.SpuSaleAttrChoice;
import com.cyh.mallportal.vo.SpuAttrVo;
import com.cyh.mallportal.vo.SpuAvailableAttrVo;

import java.util.List;
import java.util.Map;

/**
 * SPU属性管理服务接口
 * 提供商家操作SPU基本属性和销售属性的功能
 */
public interface SpuAttrService {

    /**
     * 绑定SPU基本属性
     * 商家为SPU绑定基本属性值，支持单选/多选（通过attrValueId）或手动输入（通过manualValue）
     *
     * @param dto       绑定信息
     * @param sellerId  当前商家ID
     * @return 绑定后的记录ID
     */
    Long bindBasicAttr(SpuBasicAttrBindDto dto, Long sellerId);

    /**
     * 批量绑定SPU基本属性（逐个绑定，单条失败不影响其他）
     * <p>
     * 遍历列表执行绑定，某个绑定失败不会影响其他绑定的执行。
     *
     * @param dtoList   绑定信息列表
     * @param sellerId  当前商家ID
     * @return 成功绑定的记录数
     */
    int batchBindBasicAttr(List<SpuBasicAttrBindDto> dtoList, Long sellerId);

    /**
     * 更新SPU基本属性绑定
     *
     * @param id        记录ID
     * @param dto       新的绑定信息
     * @param sellerId  当前商家ID
     * @return 是否更新成功
     */
    boolean updateBasicAttr(Long id, SpuBasicAttrBindDto dto, Long sellerId);

    /**
     * 批量更新SPU基本属性绑定
     * <p>
     * 全部校验通过后才执行更新，任一校验失败则全部回滚。
     *
     * @param dtoList   更新信息列表（需包含id、spuId、attrId）
     * @param sellerId  当前商家ID
     * @return 成功更新的记录数
     */
    int batchUpdateBasicAttr(List<SpuBasicAttrBindDto> dtoList, Long sellerId);

    /**
     * 删除SPU基本属性绑定
     *
     * @param id        记录ID
     * @param sellerId  当前商家ID
     * @return 是否删除成功
     */
    boolean deleteBasicAttr(Long id, Long sellerId);

    /**
     * 根据SPU ID获取基本属性列表
     *
     * @param spuId SPU ID
     * @return 基本属性值列表
     */
    List<SpuBasicAttrValue> getBasicAttrsBySpuId(Long spuId);

    /**
     * 绑定SPU销售属性
     * 商家为SPU绑定销售属性及可选值（如颜色：[黑,白,红]）
     *
     * @param dto       绑定信息
     * @param sellerId  当前商家ID
     * @return 绑定后的记录ID
     */
    Long bindSaleAttr(SpuSaleAttrBindDto dto, Long sellerId);

    /**
     * 批量绑定SPU销售属性
     *
     * @param dtoList   绑定信息列表
     * @param sellerId  当前商家ID
     * @return 成功绑定的记录数
     */
    int batchBindSaleAttr(List<SpuSaleAttrBindDto> dtoList, Long sellerId);

    /**
     * 更新SPU销售属性绑定
     * 如果某属性值被移除且已被SKU绑定，将抛出异常提示
     *
     * @param id        记录ID
     * @param dto       新的绑定信息
     * @param sellerId  当前商家ID
     * @return 是否更新成功
     */
    boolean updateSaleAttr(Long id, SpuSaleAttrBindDto dto, Long sellerId);

    /**
     * 批量更新SPU销售属性绑定
     * 会校验每个属性值变更，如果某属性值被移除且已被SKU绑定，全部更新失败并提示
     *
     * @param dtoList   更新信息列表
     * @param sellerId  当前商家ID
     * @return 成功更新的记录数
     */
    int batchUpdateSaleAttr(List<SpuSaleAttrBindDto> dtoList, Long sellerId);

    /**
     * 删除SPU销售属性绑定
     *
     * @param id        记录ID
     * @param sellerId  当前商家ID
     * @return 是否删除成功
     */
    boolean deleteSaleAttr(Long id, Long sellerId);

    /**
     * 根据SPU ID获取销售属性列表
     *
     * @param spuId SPU ID
     * @return 销售属性选择列表
     */
    List<SpuSaleAttrChoice> getSaleAttrsBySpuId(Long spuId);

    /**
     * 根据SPU ID获取销售属性列表（包含属性值）
     *
     * @param spuId SPU ID
     * @return 销售属性详情列表（包含属性值）
     */
    List<com.cyh.mallportal.vo.SpuAttrVo.SpuSaleAttrDetailVo> getSaleAttrsWithValuesBySpuId(Long spuId);

    /**
     * 获取SPU的所有属性（基本属性+销售属性）
     *
     * @param spuId SPU ID
     * @return 包含基本属性和销售属性的完整信息
     */
    SpuAttrVo getAllAttrsBySpuId(Long spuId);

    /**
     * 获取SPU可绑定的属性列表
     * 根据SPU的分类，显示分类下的所有属性，并标记哪些已绑定
     *
     * @param spuId SPU ID
     * @return 可用属性列表
     */
    SpuAvailableAttrVo getAvailableAttrsBySpuId(Long spuId);

    /**
     * 一次性为SPU绑定所有属性（基本属性+销售属性）
     * 会先清除该SPU原有的绑定，再批量绑定新的
     *
     * @param dto      完整属性绑定信息
     * @param sellerId 当前商家ID
     * @return 绑定结果
     */
    Map<String, Object> bindAllAttrs(SpuAttrFullBindDto dto, Long sellerId);
}
