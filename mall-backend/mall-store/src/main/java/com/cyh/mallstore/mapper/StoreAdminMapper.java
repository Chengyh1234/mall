package com.cyh.mallstore.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cyh.mallstore.entity.StoreAdmin;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 店铺管理员 Mapper 接口
 * 提供按店铺/用户查询管理员信息及批量删除操作
 */
@Mapper
public interface StoreAdminMapper extends BaseMapper<StoreAdmin> {

    /**
     * 查询指定店铺的所有管理员列表
     */
    List<StoreAdmin> selectByStoreId(@Param("storeId") Long storeId);

    /**
     * 按用户 ID 查询其管理员信息
     */
    StoreAdmin selectByUserId(@Param("userId") Long userId);

    /**
     * 按店铺 ID 和用户 ID 查询管理员（用于校验用户是否为某店铺管理员）
     */
    StoreAdmin selectByStoreIdAndUserId(@Param("storeId") Long storeId, @Param("userId") Long userId);

    /**
     * 按店铺 ID 批量删除管理员（用于注销店铺时清理管理员数据）
     */
    int deleteByStoreId(@Param("storeId") Long storeId);
}