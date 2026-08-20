package com.cyh.mallstore.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cyh.mallstore.entity.Store;
import com.cyh.mallstore.vo.StoreVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 店铺 Mapper 接口
 * 提供店铺的复杂查询（分页、VO 映射、管理端多条件筛选等）
 */
@Mapper
public interface StoreMapper extends BaseMapper<Store> {

    /**
     * 按卖家 ID 查询店铺（用于验证用户是否拥有店铺）
     */
    Store selectBySellerId(@Param("sellerId") Long sellerId);

    /**
     * 按状态查询店铺列表
     */
    List<Store> selectByStatus(@Param("status") Integer status);

    /**
     * 分页查询店铺列表（含关键字搜索、状态筛选）
     */
    List<Store> selectPage(@Param("keyword") String keyword,
                           @Param("status") Integer status,
                           @Param("offset") int offset,
                           @Param("limit") int limit);

    /**
     * 统计店铺数量（含关键字搜索、状态筛选，配合 selectPage 分页）
     */
    int countPage(@Param("keyword") String keyword,
                  @Param("status") Integer status);

    /**
     * 分页查询店铺公开 VO（前端展示，仅返回公开字段）
     */
    List<StoreVo> selectPageVO(@Param("keyword") String keyword,
                               @Param("offset") int offset,
                               @Param("limit") int limit);

    /**
     * 统计公开店铺数量（配合 selectPageVO 分页）
     */
    int countPageVO(@Param("keyword") String keyword);

    /**
     * 管理端多条件分页查询店铺（支持 ID/关键字/状态/卖家/手机号筛选）
     */
    List<Store> selectAdminPage(@Param("id") Long id,
                                @Param("keyword") String keyword,
                                @Param("status") Integer status,
                                @Param("sellerId") Long sellerId,
                                @Param("phone") String phone,
                                @Param("offset") Integer offset,
                                @Param("limit") Integer limit);

    /**
     * 统计管理端多条件查询结果数量（配合 selectAdminPage 分页）
     */
    int countAdminPage(@Param("id") Long id,
                       @Param("keyword") String keyword,
                       @Param("status") Integer status,
                       @Param("sellerId") Long sellerId,
                       @Param("phone") String phone);
}