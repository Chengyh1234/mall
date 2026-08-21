package com.cyh.mallproduct.service;

import com.cyh.mallproduct.entity.Sku;
import com.cyh.mallproduct.vo.SkuAdminVo;
import com.cyh.mallproduct.vo.SkuStoreVo;
import com.cyh.mallproduct.vo.SkuVo;

import java.util.List;

/**
 * SKU服务接口
 * 提供商品库存单元业务逻辑操作
 */
public interface SkuService {

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
     * 批量删除SKU（逻辑删除，同时删除SKU的销售属性绑定）
     *
     * @param ids SKU ID列表
     * @return 删除成功数量
     */
    int batchDelete(List<Long> ids);

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
    //List<Sku> getPage(Long spuId, Integer status, Integer page, Integer pageSize);

    /**
     * 获取SKU总数
     *
     * @param spuId  SPU ID（可选）
     * @param status 状态（可选）
     * @return 总数
     */
    int count(Long spuId, Integer status);

    /**
     * 启用SKU（设置 status=1）
     *
     * @param id SKU ID
     * @return 是否启用成功
     */
    boolean enable(Long id);

    /**
     * 禁用SKU（设置 status=0）
     *
     * @param id SKU ID
     * @return 是否禁用成功
     */
    boolean disable(Long id);

    //    /**
//     * 获取SPU的最低价格
//     *
//     * @param spuId SPU ID
//     * @return 最低价格
//     */
//    BigDecimal getMinPrice(Long spuId);

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
     * @return SKU列表（包含销售属性，公开字段）
     */
    List<SkuVo> getBySpuIdWithAttributes(Long spuId);

    /**
     * 商家端：根据SPU ID获取SKU列表（包含销售属性）
     * 返回商家经营管理所需的完整字段
     *
     * @param spuId SPU ID
     * @return SKU列表（包含销售属性，商家端字段）
     */
    List<SkuStoreVo> getStoreBySpuIdWithAttributes(Long spuId);

    /**
     * 管理员端：根据SPU ID获取SKU列表（包含销售属性）
     * 返回管理员监管所需的全部字段
     *
     * @param spuId SPU ID
     * @return SKU列表（包含销售属性，管理员端字段）
     */
    List<SkuAdminVo> getAdminBySpuIdWithAttributes(Long spuId);

    /**
     * 根据ID获取SKU详情（包含销售属性）
     *
     * @param id SKU ID
     * @return SKU详情（包含销售属性）
     */
    //SkuVo getByIdWithAttributes(Long id);
}