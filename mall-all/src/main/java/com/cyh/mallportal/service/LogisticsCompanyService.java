package com.cyh.mallportal.service;

import com.cyh.mallportal.entity.LogisticsCompany;

import java.util.List;

/**
 * 物流公司服务接口
 * 提供物流公司业务逻辑操作
 */
public interface LogisticsCompanyService {

    /**
     * 获取所有启用的物流公司列表
     *
     * @return 物流公司列表
     */
    List<LogisticsCompany> getEnabledList();

    /**
     * 根据ID获取物流公司
     *
     * @param id 物流公司ID
     * @return 物流公司
     */
    LogisticsCompany getById(Long id);

    /**
     * 根据代码获取物流公司
     *
     * @param code 物流公司代码
     * @return 物流公司
     */
    LogisticsCompany getByCode(String code);

    /**
     * 获取物流公司列表（分页）
     *
     * @param status 状态（可选）
     * @param page   页码
     * @param size   每页条数
     * @return 物流公司列表
     */
    List<LogisticsCompany> getPage(Integer status, Integer page, Integer size);

    /**
     * 获取物流公司总数
     *
     * @param status 状态（可选）
     * @return 总数
     */
    int count(Integer status);

    /**
     * 新增物流公司
     *
     * @param company 物流公司实体
     * @return 是否成功
     */
    boolean add(LogisticsCompany company);

    /**
     * 更新物流公司
     *
     * @param company 物流公司实体
     * @return 是否成功
     */
    boolean update(LogisticsCompany company);

    /**
     * 删除物流公司
     *
     * @param id 物流公司ID
     * @return 是否成功
     */
    boolean delete(Long id);

    /**
     * 更新状态
     *
     * @param id     物流公司ID
     * @param status 状态
     * @return 是否成功
     */
    boolean updateStatus(Long id, Integer status);
}