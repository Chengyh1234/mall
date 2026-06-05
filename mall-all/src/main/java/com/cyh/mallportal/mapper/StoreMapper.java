package com.cyh.mallportal.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cyh.mallportal.entity.Store;
import com.cyh.mallportal.vo.StoreVO;
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

    /**
     * 分页查询公开店铺列表（仅 status=1），返回 StoreVO
     * 用于无需权限的店铺展示页面
     *
     * @param keyword 搜索关键字（店铺名称模糊匹配）
     * @param offset  偏移量
     * @param limit   每页数量
     * @return 店铺 VO 列表
     */
    List<StoreVO> selectPageVO(@Param("keyword") String keyword,
                               @Param("offset") Integer offset,
                               @Param("limit") Integer limit);

    /**
     * 统计公开店铺数量（仅 status=1）
     *
     * @param keyword 搜索关键字
     * @return 店铺数量
     */
    int countPageVO(@Param("keyword") String keyword);

    /**
     * 管理员分页查询店铺列表（多条件筛选），返回完整 Store 实体
     * 支持按店铺ID、名称关键字、状态、商家ID、联系电话搜索
     *
     * @param id       店铺ID（精确匹配，可选）
     * @param keyword  店铺名称关键字（模糊匹配，可选）
     * @param status   店铺状态（可选）
     * @param sellerId 商家用户ID（精确匹配，可选）
     * @param phone    联系电话（模糊匹配，可选）
     * @param offset   偏移量
     * @param limit    每页数量
     * @return 店铺列表
     */
    List<Store> selectAdminPage(@Param("id") Long id,
                                @Param("keyword") String keyword,
                                @Param("status") Integer status,
                                @Param("sellerId") Long sellerId,
                                @Param("phone") String phone,
                                @Param("offset") Integer offset,
                                @Param("limit") Integer limit);

    /**
     * 管理员统计店铺总数（多条件筛选）
     * 与 selectAdminPage 条件完全一致
     *
     * @param id       店铺ID（精确匹配，可选）
     * @param keyword  店铺名称关键字（模糊匹配，可选）
     * @param status   店铺状态（可选）
     * @param sellerId 商家用户ID（精确匹配，可选）
     * @param phone    联系电话（模糊匹配，可选）
     * @return 店铺总数
     */
    int countAdminPage(@Param("id") Long id,
                       @Param("keyword") String keyword,
                       @Param("status") Integer status,
                       @Param("sellerId") Long sellerId,
                       @Param("phone") String phone);
}