package com.cyh.mallportal.service;

import com.cyh.mallportal.entity.Spu;
import com.cyh.mallportal.vo.SpuDetailVo;

import java.util.List;

/**
 * 商品SPU服务接口
 * 定义商品的新增、删除、修改、查询等业务方法
 */
public interface SpuService {

    /**
     * 新增商品
     * @param spu 商品信息
     * @return 新增的商品ID，失败返回null
     */
    Long add(Spu spu);

    /**
     * 根据ID删除商品
     * @param id 商品ID
     * @return 删除成功返回true，失败返回false
     */
    boolean delete(Long id);

    /**
     * 更新商品信息
     * @param spu 商品信息（需包含ID）
     * @return 更新成功返回true，失败返回false
     */
    boolean update(Spu spu);

    /**
     * 根据ID获取商品详情
     * @param id 商品ID
     * @return 商品信息，不存在返回null
     */
    Spu getById(Long id);

    /**
     * 获取商品列表（不分页）
     * @param spu 查询条件（支持id、categoryId、brandId、name、status）
     * @return 商品列表，按创建时间倒序排列
     */
    List<Spu> getList(Spu spu);

    /**
     * 分页查询商品
     * @param spu 查询条件（支持id、categoryId、brandId、name、status）
     * @param page 页码，默认第1页
     * @param pageSize 每页数量，默认10条
     * @return 商品列表，按创建时间倒序排列
     */
    List<Spu> getPage(Spu spu, Integer page, Integer pageSize);

    /**
     * 分页查询商品（支持分类及其子分类）
     * 当categoryId不为空时，会查询该分类及其所有子分类关联的商品
     * @param spu 查询条件（支持id、categoryId、brandId、name、status）
     * @param page 页码，默认第1页
     * @param pageSize 每页数量，默认10条
     * @param categoryIds 分类ID列表（包含分类及其子分类）
     * @return 商品列表，按创建时间倒序排列
     */
    List<Spu> getPageByCategoryIds(Spu spu, Integer page, Integer pageSize, List<Long> categoryIds);

    /**
     * 分页搜索商品（支持多字段模糊搜索：商品名称、分类名称、品牌名称）
     *
     * @param categoryIds 分类ID列表（可选，包含分类及其子分类）
     * @param keyword 搜索关键字（可选，匹配商品名称、分类名称、品牌名称）
     * @param brandId 品牌ID（可选）
     * @param page 页码
     * @param pageSize 每页数量
     * @return 商品列表
     */
    List<Spu> searchByKeyword(List<Long> categoryIds, String keyword, Long brandId, Integer page, Integer pageSize);

    /**
     * 统计搜索结果数量
     *
     * @param categoryIds 分类ID列表（可选）
     * @param keyword 搜索关键字（可选）
     * @param brandId 品牌ID（可选）
     * @return 商品数量
     */
    int countSearchByKeyword(List<Long> categoryIds, String keyword, Long brandId);

    /**
     * 根据商家ID获取商品列表（不分页）
     * 用于商家管理自己的商品
     * @param sellerId 商家ID
     * @return 商品列表
     */
    List<Spu> getListBySellerId(Long sellerId);

    /**
     * 根据商家ID分页获取商品列表
     * 用于商家管理自己的商品
     * @param sellerId 商家ID
     * @param status 状态（可选，1-上架 0-下架）
     * @param keyword 关键字（可选，按商品名称模糊搜索）
     * @param page 页码
     * @param pageSize 每页数量
     * @return 商品列表
     */
    List<Spu> getPageBySellerId(Long sellerId, Integer status, String keyword, Integer page, Integer pageSize);

    /**
     * 统计商家商品数量
     * @param sellerId 商家ID
     * @param status 状态（可选）
     * @param keyword 关键字（可选）
     * @return 商品数量
     */
    int countBySellerId(Long sellerId, Integer status, String keyword);

    /**
     * 根据商家ID获取商品详情
     * 用于商家查看自己商品的详情
     * @param id 商品ID
     * @param sellerId 商家ID（用于权限校验）
     * @return 商品信息，不存在返回null
     */
    Spu getByIdAndSellerId(Long id, Long sellerId);

    /**
     * 获取商品详情（包含商家信息）
     * 用于前端展示商品详情页
     * @param id 商品ID
     * @return 商品详情VO，包含商品信息和商家信息
     */
    SpuDetailVo getSpuDetailById(Long id);

    /**
     * 恢复被逻辑删除的商品（设置 is_deleted=0）
     * @param id 商品ID
     * @return 恢复成功返回true，失败返回false
     */
    boolean restore(Long id);

    /**
     * 更新SPU的最低SKU售价（从该SPU下所有启用状态的SKU中取最低price）
     * @param spuId SPU ID
     */
    void updateMinPriceForSpu(Long spuId);

    /**
     * 检查SPU下是否存在启用状态（status=1）的SKU
     *
     * @param spuId SPU ID
     * @return true=存在启用SKU，false=不存在
     */
    boolean hasEnabledSku(Long spuId);

    /**
     * 【运营管理员】分页获取全部商品列表（含上架和下架）
     * 不限商家，用于运营管理员查看全平台商品
     *
     * @param status   状态（可选，1-上架 0-下架，不传则查询全部）
     * @param keyword  关键字（可选，按商品名称模糊搜索）
     * @param page     页码
     * @param pageSize 每页数量
     * @return 商品列表（含 categoryName、brandName）
     */
    List<Spu> getPageAll(Integer status, String keyword, Integer page, Integer pageSize);

    /**
     * 【运营管理员】统计全部商品数量
     *
     * @param status  状态（可选，不传统计全部）
     * @param keyword 关键字（可选）
     * @return 商品数量
     */
    int countAll(Integer status, String keyword);
}
