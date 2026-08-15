package com.cyh.mallportal.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.cyh.mallportal.entity.Brand;

import java.util.List;

/**
 * 品牌服务接口
 * 定义品牌的新增、删除、修改、查询等业务方法
 */
public interface BrandService {

    /**
     * 新增品牌
     *
     * @param brand 品牌信息
     * @return 新增的品牌ID，失败返回null
     */
    Long add(Brand brand);

    /**
     * 根据ID删除品牌（软删除）
     *
     * @param id 品牌ID
     * @return 删除成功返回true，失败返回false
     */
    boolean delete(Long id);

    /**
     * 更新品牌信息
     *
     * @param brand 品牌信息（需包含ID）
     * @return 更新成功返回true，失败返回false
     */
    boolean update(Brand brand);

    /**
     * 根据ID获取品牌详情
     *
     * @param id 品牌ID
     * @return 品牌信息，不存在返回null
     */
    Brand getById(Long id);

    /**
     * 获取品牌列表（不分页）
     *
     * @param brand 查询条件（支持id、name、status）
     * @return 品牌列表
     */
    List<Brand> getList(Brand brand);

    /**
     * 分页查询品牌
     *
     * @param brand 查询条件（支持id、name、status）
     * @param page 页码，默认第1页
     * @param pageSize 每页数量，默认10条
     * @return 品牌分页列表
     */
    IPage<Brand> getPage(Brand brand, Integer page, Integer pageSize);

    /**
     * 根据状态查询品牌列表
     *
     * @param status 状态（1-启用 0-禁用）
     * @return 品牌列表，按排序号升序、创建时间降序排列
     */
    List<Brand> getByStatus(Integer status);

    /**
     * 根据品牌名称模糊查询品牌列表
     *
     * @param name 品牌名称（支持模糊匹配）
     * @return 品牌列表，只返回启用状态的品牌
     */
    List<Brand> getByNameLike(String name);

    /**
     * 按排序号查询品牌列表
     *
     * @return 品牌列表，只返回启用状态的品牌，按排序号升序排列
     */
    List<Brand> getBySort();
}
