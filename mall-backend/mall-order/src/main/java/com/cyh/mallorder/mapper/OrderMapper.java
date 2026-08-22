package com.cyh.mallorder.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cyh.mallorder.entity.Order;
import com.cyh.mallorder.vo.OrderStatusCountVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单数据访问层
 * 提供订单的增删改查及统计功能
 */
@Mapper
public interface OrderMapper extends BaseMapper<Order> {

    /**
     * 根据订单号查询订单
     */
    Order selectByOrderNo(@Param("orderNo") String orderNo);

    /**
     * 分页查询用户订单列表
     */
    IPage<Order> selectByUserIdPaged(Page<?> page, @Param("userId") Long userId);

    /**
     * 按状态分页查询用户订单列表
     */
    IPage<Order> selectByUserIdAndStatusPaged(Page<?> page, @Param("userId") Long userId, @Param("status") Integer status);

    /**
     * 统计用户订单总数
     */
    int countByUserId(@Param("userId") Long userId);

    /**
     * 统计用户指定状态订单数量
     */
    int countByUserIdAndStatus(@Param("userId") Long userId, @Param("status") Integer status);

    /**
     * 批量统计用户各状态订单数量
     */
    OrderStatusCountVo countOrderStatusByUserId(@Param("userId") Long userId);

    /**
     * 批量查询属于该用户的订单
     */
    List<Order> selectByIdsAndUserId(@Param("orderIds") List<Long> orderIds, @Param("userId") Long userId);

    /**
     * 分页查询商家店铺订单列表
     */
    IPage<Order> selectBySellerId(Page<?> page, @Param("sellerId") Long sellerId);

    /**
     * 按状态分页查询商家店铺订单列表
     */
    IPage<Order> selectBySellerIdAndStatus(Page<?> page, @Param("sellerId") Long sellerId, @Param("status") Integer status);

    /**
     * 统计商家店铺订单总数
     */
    int countBySellerId(@Param("sellerId") Long sellerId);

    /**
     * 统计商家店铺指定状态订单总数
     */
    int countBySellerIdAndStatus(@Param("sellerId") Long sellerId, @Param("status") Integer status);

    /**
     * 商家分页查询店铺订单列表（多条件筛选）
     */
    IPage<Order> selectBySellerIdWithFilters(Page<?> page,
                                             @Param("sellerId") Long sellerId,
                                             @Param("status") Integer status,
                                             @Param("userId") Long userId,
                                             @Param("orderNo") String orderNo,
                                             @Param("payTimeStart") LocalDateTime payTimeStart,
                                             @Param("payTimeEnd") LocalDateTime payTimeEnd,
                                             @Param("deliveryTimeStart") LocalDateTime deliveryTimeStart,
                                             @Param("deliveryTimeEnd") LocalDateTime deliveryTimeEnd,
                                             @Param("receiveTimeStart") LocalDateTime receiveTimeStart,
                                             @Param("receiveTimeEnd") LocalDateTime receiveTimeEnd);

    /**
     * 统计商家店铺订单总数（多条件筛选）
     */
    int countBySellerIdWithFilters(@Param("sellerId") Long sellerId,
                                   @Param("status") Integer status,
                                   @Param("userId") Long userId,
                                   @Param("orderNo") String orderNo,
                                   @Param("payTimeStart") LocalDateTime payTimeStart,
                                   @Param("payTimeEnd") LocalDateTime payTimeEnd,
                                   @Param("deliveryTimeStart") LocalDateTime deliveryTimeStart,
                                   @Param("deliveryTimeEnd") LocalDateTime deliveryTimeEnd,
                                   @Param("receiveTimeStart") LocalDateTime receiveTimeStart,
                                   @Param("receiveTimeEnd") LocalDateTime receiveTimeEnd);

    /**
     * 根据订单号和商家ID查询订单
     */
    Order selectByOrderNoAndSellerId(@Param("orderNo") String orderNo, @Param("sellerId") Long sellerId);

    /**
     * 管理员根据订单号查询订单（不过滤 is_deleted）
     */
    Order selectByOrderNoForAdmin(@Param("orderNo") String orderNo);

    /**
     * 管理员分页查询全部订单（多条件筛选）
     */
    IPage<Order> selectAllOrdersWithFilters(Page<?> page,
                                            @Param("status") Integer status,
                                            @Param("userId") Long userId,
                                            @Param("orderNo") String orderNo,
                                            @Param("payTimeStart") LocalDateTime payTimeStart,
                                            @Param("payTimeEnd") LocalDateTime payTimeEnd,
                                            @Param("deliveryTimeStart") LocalDateTime deliveryTimeStart,
                                            @Param("deliveryTimeEnd") LocalDateTime deliveryTimeEnd,
                                            @Param("receiveTimeStart") LocalDateTime receiveTimeStart,
                                            @Param("receiveTimeEnd") LocalDateTime receiveTimeEnd);

    /**
     * 统计全部订单总数（多条件筛选）
     */
    int countAllOrdersWithFilters(@Param("status") Integer status,
                                  @Param("userId") Long userId,
                                  @Param("orderNo") String orderNo,
                                  @Param("payTimeStart") LocalDateTime payTimeStart,
                                  @Param("payTimeEnd") LocalDateTime payTimeEnd,
                                  @Param("deliveryTimeStart") LocalDateTime deliveryTimeStart,
                                  @Param("deliveryTimeEnd") LocalDateTime deliveryTimeEnd,
                                  @Param("receiveTimeStart") LocalDateTime receiveTimeStart,
                                  @Param("receiveTimeEnd") LocalDateTime receiveTimeEnd);
}