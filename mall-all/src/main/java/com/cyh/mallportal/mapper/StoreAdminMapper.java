package com.cyh.mallportal.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cyh.mallportal.entity.StoreAdmin;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 店铺管理员Mapper接口
 */
@Mapper
public interface StoreAdminMapper extends BaseMapper<StoreAdmin> {

    /**
     * 根据店铺ID查询管理员列表
     *
     * @param storeId 店铺ID
     * @return 管理员列表
     */
    List<StoreAdmin> selectByStoreId(@Param("storeId") Long storeId);

    /**
     * 根据用户ID查询管理员记录
     *
     * @param userId 用户ID
     * @return 管理员记录
     */
    StoreAdmin selectByUserId(@Param("userId") Long userId);

    /**
     * 检查用户是否为店铺管理员
     *
     * @param storeId 店铺ID
     * @param userId 用户ID
     * @return 管理员记录
     */
    StoreAdmin selectByStoreIdAndUserId(@Param("storeId") Long storeId, @Param("userId") Long userId);

    /**
     * 根据店铺ID删除所有管理员
     *
     * @param storeId 店铺ID
     * @return 删除数量
     */
    int deleteByStoreId(@Param("storeId") Long storeId);
}