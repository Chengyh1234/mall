package com.cyh.mallportal.service;

import com.cyh.mallportal.entity.Spu;

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
}
