package com.cyh.mallportal.service;

import com.cyh.mallportal.entity.Store;
import com.cyh.mallportal.vo.StoreDetailVO;
import com.cyh.mallportal.vo.StoreVO;

import java.util.List;

/**
 * 店铺服务接口
 */
public interface StoreService {

    /**
     * 新增店铺
     * @param store 店铺信息
     * @return 新增的店铺ID，失败返回null
     */
    Long add(Store store);

    /**
     * 更新店铺信息
     * @param store 店铺信息（需包含ID）
     * @return 更新成功返回true，失败返回false
     */
    boolean update(Store store);

    /**
     * 根据ID获取店铺详情
     * @param id 店铺ID
     * @return 店铺信息，不存在返回null
     */
    Store getById(Long id);

    /**
     * 根据ID查询店铺详情（返回 StoreDetailVO）
     * 用于公开展示店铺详细信息，不包含内部管理字段
     *
     * @param id 店铺ID
     * @return 店铺详情 VO，店铺不存在返回 null
     */
    StoreDetailVO getDetailVO(Long id);

    /**
     * 根据商家ID获取店铺
     * @param sellerId 商家用户ID
     * @return 店铺信息
     */
    Store getBySellerId(Long sellerId);

    /**
     * 获取店铺列表
     * @param store 查询条件
     * @return 店铺列表
     */
    List<Store> getList(Store store);

    /**
     * 分页获取店铺列表
     * @param keyword 搜索关键字
     * @param status 状态筛选
     * @param page 页码
     * @param pageSize 每页数量
     * @return 店铺列表
     */
    List<Store> getPage(String keyword, Integer status, Integer page, Integer pageSize);

    /**
     * 统计分页数量
     * @param keyword 搜索关键字
     * @param status 状态筛选
     * @return 总数量
     */
    int countPage(String keyword, Integer status);

    /**
     * 分页查询公开店铺列表（仅 status=1）
     * 用于无需权限的店铺展示页面，返回 StoreVO，不包含内部管理字段
     *
     * @param keyword  搜索关键字（店铺名称模糊匹配）
     * @param page     页码，从1开始
     * @param pageSize 每页数量
     * @return 店铺 VO 分页列表
     */
    List<StoreVO> getPageVO(String keyword, Integer page, Integer pageSize);

    /**
     * 统计公开店铺数量（仅 status=1）
     *
     * @param keyword 搜索关键字
     * @return 店铺总数
     */
    int countPageVO(String keyword);

    /**
     * 管理员分页查询店铺列表（多条件筛选）
     * 可查看全部状态的店铺，支持按店铺ID、名称关键字、商家ID、联系电话搜索
     *
     * @param id       店铺ID（精确匹配，可选）
     * @param keyword  店铺名称关键字（模糊匹配，可选）
     * @param status   店铺状态（可选，不传查全部）
     * @param sellerId 商家用户ID（精确匹配，可选）
     * @param phone    联系电话（模糊匹配，可选）
     * @param page     页码，从1开始
     * @param pageSize 每页数量
     * @return 店铺列表（完整 Store 实体）
     */
    List<Store> getAdminPage(Long id, String keyword, Integer status,
                             Long sellerId, String phone,
                             Integer page, Integer pageSize);

    /**
     * 管理员统计店铺总数（多条件筛选）
     *
     * @param id       店铺ID（精确匹配，可选）
     * @param keyword  店铺名称关键字（模糊匹配，可选）
     * @param status   店铺状态（可选）
     * @param sellerId 商家用户ID（精确匹配，可选）
     * @param phone    联系电话（模糊匹配，可选）
     * @return 店铺总数
     */
    int countAdminPage(Long id, String keyword, Integer status,
                       Long sellerId, String phone);

    /**
     * 更新店铺状态
     * @param id 店铺ID
     * @param status 状态
     * @return 更新成功返回true
     */
    boolean updateStatus(Long id, Integer status);

    /**
     * 检查用户是否为店铺店长
     * @param storeId 店铺ID
     * @param userId 用户ID
     * @return 是否为店长
     */
    boolean isStoreOwner(Long storeId, Long userId);

    /**
     * 检查用户是否为店铺管理员
     * @param storeId 店铺ID
     * @param userId 用户ID
     * @return 是否为管理员
     */
    boolean isStoreAdmin(Long storeId, Long userId);

    /**
     * 检查用户是否有店铺操作权限
     * @param storeId 店铺ID
     * @param userId 用户ID
     * @return 是否有权限
     */
    boolean hasStorePermission(Long storeId, Long userId);

    // ========== 开店申请 ==========

    /**
     * 用户提交开店申请
     * 校验用户无SELLER角色、无审核中的申请、店铺名唯一后，插入status=2(审核中)的店铺
     *
     * @param userId      申请人用户ID
     * @param name        店铺名称
     * @param description 店铺描述
     * @param phone       联系电话
     * @param address     店铺地址
     * @return 创建的店铺记录
     */
    Store apply(Long userId, String name, String description, String phone, String address);

    /**
     * 管理员审核通过开店申请
     * 将店铺status改为1(正常)，并给用户赋予SELLER角色
     *
     * @param id 店铺ID
     */
    void approve(Long id);

    /**
     * 管理员驳回开店申请
     * 将店铺status改为3(审核失败)，记录驳回原因
     *
     * @param id           店铺ID
     * @param rejectReason 驳回原因
     */
    void reject(Long id, String rejectReason);

    /**
     * 用户重新提交开店申请（驳回后修改重新提交）
     * 校验用户有status=3(审核失败)的店铺，更新信息并重置status=2(审核中)
     *
     * @param storeId     店铺ID
     * @param name        店铺名称
     * @param description 店铺描述
     * @param phone       联系电话
     * @param address     店铺地址
     */
    void reSubmit(Long storeId, String name, String description, String phone, String address);

    /**
     * 用户查看自己的开店申请
     *
     * @param userId 用户ID
     * @return 店铺记录（可能为null）
     */
    Store getMyApply(Long userId);

    /**
     * 管理员分页查询待审核列表（status=2）
     *
     * @param page     页码
     * @param pageSize 每页数量
     * @return 待审核店铺列表
     */
    List<Store> getPendingPage(Integer page, Integer pageSize);

    /**
     * 统计待审核数量
     *
     * @return 待审核总数
     */
    int countPending();

    /**
     * 商家注销店铺
     * 校验店铺下所有订单均为完结状态（已完成/已取消/已退款）后方可注销，
     * 注销后店铺 status 置为 0（禁用/已注销）
     *
     * @param storeId 店铺ID
     * @param userId  操作人用户ID（需为店铺所有者）
     */
    void deactivate(Long storeId, Long userId);
}