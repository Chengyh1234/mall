package com.cyh.mallportal.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cyh.mallportal.entity.Store;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 店铺Mapper接口
 */
@Mapper
public interface StoreMapper extends BaseMapper<Store> {

    /**
     * 根据商家ID查询店铺
     *
     * @param sellerId 商家用户ID
     * @return 店铺信息
     */
    Store selectBySellerId(@Param("sellerId") Long sellerId);

    /**
     * 根据状态查询店铺列表
     *
     * @param status 状态: 1-正常 0-禁用 2-审核中 3-审核失败
     * @return 店铺列表
     */
    List<Store> selectByStatus(@Param("status") Integer status);

    /**
     * 分页查询店铺列表
     *
     * @param keyword 搜索关键字（店铺名称）
     * @param status 状态筛选
     * @param offset 偏移量
     * @param limit 每页数量
     * @return 店铺列表
     */
    List<Store> selectPage(@Param("keyword") String keyword,
                          @Param("status") Integer status,
                          @Param("offset") Integer offset,
                          @Param("limit") Integer limit);

    /**
     * 统计店铺数量
     *
     * @param keyword 搜索关键字
     * @param status 状态筛选
     * @return 店铺数量
     */
    int countPage(@Param("keyword") String keyword, @Param("status") Integer status);
}