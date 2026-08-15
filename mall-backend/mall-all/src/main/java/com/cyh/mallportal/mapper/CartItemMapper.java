package com.cyh.mallportal.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cyh.mallportal.entity.CartItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 购物车Mapper接口
 * 提供购物车数据访问操作
 */
@Mapper
public interface CartItemMapper extends BaseMapper<CartItem> {

    /**
     * 根据用户ID查询购物车列表
     *
     * @param userId 用户ID
     * @return 购物车列表
     */
    List<CartItem> selectByUserId(@Param("userId") Long userId);

    /**
     * 根据用户ID查询已选中的购物车列表
     *
     * @param userId 用户ID
     * @return 已选中的购物车列表
     */
    List<CartItem> selectSelectedByUserId(@Param("userId") Long userId);

    /**
     * 根据用户ID和SKU ID查询购物车项
     *
     * @param userId 用户ID
     * @param skuId  SKU ID
     * @return 购物车项
     */
    CartItem selectByUserIdAndSkuId(@Param("userId") Long userId, @Param("skuId") Long skuId);

    /**
     * 获取用户购物车商品数量
     *
     * @param userId 用户ID
     * @return 商品数量
     */
    int countByUserId(@Param("userId") Long userId);

    /**
     * 获取用户已选中商品数量
     *
     * @param userId 用户ID
     * @return 已选中商品数量
     */
    int countSelectedByUserId(@Param("userId") Long userId);

    /**
     * 清空用户购物车
     *
     * @param userId 用户ID
     * @return 删除数量
     */
    int deleteByUserId(@Param("userId") Long userId);

    /**
     * 清空用户已选中的购物车商品
     *
     * @param userId 用户ID
     * @return 删除数量
     */
    int deleteSelectedByUserId(@Param("userId") Long userId);
}