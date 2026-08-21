package com.cyh.mallproduct.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cyh.mallproduct.entity.Spu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品SPU Mapper接口
 */
@Mapper
public interface SpuMapper extends BaseMapper<Spu> {

    /**
     * 根据分类ID查询商品列表
     *
     * @param categoryId 分类ID
     * @return 商品列表
     */
    List<Spu> selectByCategoryId(@Param("categoryId") Long categoryId);

    /**
     * 根据分类ID统计商品数量
     *
     * @param categoryId 分类ID
     * @return 商品数量
     */
    int countByCategoryId(@Param("categoryId") Long categoryId);

    /**
     * 根据分类ID查询所有商品（包含已下架的）
     *
     * @param categoryId 分类ID
     * @return 商品列表
     */
    List<Spu> selectAllByCategoryId(@Param("categoryId") Long categoryId);

    /**
     * 分页搜索商品（支持多字段模糊搜索：商品名称、分类名称、品牌名称）
     *
     * @param categoryIds 分类ID列表（可选，包含分类及其子分类）
     * @param keyword 搜索关键字（可选，匹配商品名称、分类名称、品牌名称）
     * @param brandId 品牌ID（可选）
     * @param offset 分页偏移量
     * @param limit 每页数量
     * @return 商品列表
     */
    List<Spu> searchByKeyword(@Param("categoryIds") List<Long> categoryIds,
                              @Param("keyword") String keyword,
                              @Param("brandId") Long brandId,
                              @Param("offset") Integer offset,
                              @Param("limit") Integer limit);

    /**
     * 统计搜索结果数量
     *
     * @param categoryIds 分类ID列表（可选）
     * @param keyword 搜索关键字（可选）
     * @param brandId 品牌ID（可选）
     * @return 商品数量
     */
    int countSearchByKeyword(@Param("categoryIds") List<Long> categoryIds,
                             @Param("keyword") String keyword,
                             @Param("brandId") Long brandId);

    /**
     * 累加 SPU 销量（原子操作）
     *
     * @param spuId    商品SPU ID
     * @param quantity 销量增量
     * @return 影响行数
     */
    int increaseSales(@Param("spuId") Long spuId, @Param("quantity") Integer quantity);
}