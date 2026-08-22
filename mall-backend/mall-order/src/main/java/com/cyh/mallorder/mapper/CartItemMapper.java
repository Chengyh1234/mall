package com.cyh.mallorder.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cyh.mallorder.entity.CartItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 购物车数据访问层
 */
@Mapper
public interface CartItemMapper extends BaseMapper<CartItem> {

    /**
     * 根据用户ID查询购物车列表
     */
    List<CartItem> selectByUserId(@Param("userId") Long userId);

    /**
     * 根据用户ID和SKU ID查询购物车项
     */
    CartItem selectByUserIdAndSkuId(@Param("userId") Long userId, @Param("skuId") Long skuId);

    /**
     * 查询用户已选中的购物车商品
     */
    List<CartItem> selectSelectedByUserId(@Param("userId") Long userId);

    /**
     * 统计用户购物车商品种数
     */
    int countByUserId(@Param("userId") Long userId);

    /**
     * 删除用户所有购物车商品
     */
    int deleteByUserId(@Param("userId") Long userId);

    /**
     * 删除用户已选中的购物车商品
     */
    int deleteSelectedByUserId(@Param("userId") Long userId);
}