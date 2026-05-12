package com.cyh.mallportal.service;

import com.cyh.mallportal.entity.Sku;
import com.cyh.mallportal.vo.SkuVo;

import java.math.BigDecimal;
import java.util.List;

/**
 * SKU服务接口
 * 提供商品库存单元业务逻辑操作
 */
public interface SkuService {

    /**
     * 新增SKU
     *
     * @param sku SKU实体
     * @return SKU ID
     */
    Long add(Sku sku);

    /**
     * 批量新增SKU
     *
     * @param skus SKU列表
     * @return 是否成功
     */
    boolean batchAdd(List<Sku> skus);

    /**
     * 删除SKU
     *
     * @param id SKU ID
     * @return 是否删除成功
     */
    boolean delete(Long id);

    /**
     * 根据SPU ID删除所有SKU
     *
     * @param spuId SPU ID
     * @return 是否删除成功
     */
    boolean deleteBySpuId(Long spuId);

    /**
     * 更新SKU信息
     *
     * @param sku SKU实体
     * @return 是否更新成功
     */
    boolean update(Sku sku);

    /**
     * 更新库存
     *
     * @param id     SKU ID
     * @param stock  库存数量
     * @return 是否更新成功
     */
    boolean updateStock(Long id, Integer stock);

    /**
     * 扣减库存
     *
     * @param id       SKU ID
     * @param quantity 扣减数量
     * @return 是否扣减成功
     */
    boolean decreaseStock(Long id, Integer quantity);

    /**
     * 根据ID获取SKU详情
     *
     * @param id SKU ID
     * @return SKU实体
     */
    Sku getById(Long id);

    /**
     * 根据SPU ID获取SKU列表
     *
     * @param spuId SPU ID
     * @return SKU列表
     */
    List<Sku> getBySpuId(Long spuId);

    /**
     * 根据SKU编码获取SKU
     *
     * @param skuCode SKU编码
     * @return SKU实体
     */
    Sku getBySkuCode(String skuCode);

    /**
     * 获取所有SKU列表
     *
     * @return SKU列表
     */
    List<Sku> getAll();

    /**
     * 获取SKU列表（分页）
     *
     * @param spuId   SPU ID（可选）
     * @param status  状态（可选）
     * @param page    页码
     * @param pageSize 每页条数
     * @return SKU列表
     */
    List<Sku> getPage(Long spuId, Integer status, Integer page, Integer pageSize);

    /**
     * 获取SKU总数
     *
     * @param spuId  SPU ID（可选）
     * @param status 状态（可选）
     * @return 总数
     */
    int count(Long spuId, Integer status);

    /**
     * 获取SPU的最低价格
     *
     * @param spuId SPU ID
     * @return 最低价格
     */
    BigDecimal getMinPrice(Long spuId);

    /**
     * 获取SPU的库存总量
     *
     * @param spuId SPU ID
     * @return 库存总量
     */
    Integer getTotalStock(Long spuId);

    /**
     * 根据SPU ID获取SKU列表（包含销售属性）
     *
     * @param spuId SPU ID
     * @return SKU列表（包含销售属性）
     */
    List<SkuVo> getBySpuIdWithAttributes(Long spuId);

    /**
     * 根据ID获取SKU详情（包含销售属性）
     *
     * @param id SKU ID
     * @return SKU详情（包含销售属性）
     */
    SkuVo getByIdWithAttributes(Long id);
}
